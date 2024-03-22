package com.ict.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ict.dao.VO;
import com.ict.service.GuestBook2Service;

@Controller
public class GuestBook2Controller {
	@Autowired
	private GuestBook2Service guestBook2Service;
	
	// 암호는 스프링security에서 지원하므로 pom.xml(164행)에 추가해야 된다.
	// (spring bean config~~ file) spring-security.xml를 만들어서 bean 생성 
	// web.xml에서 spring-security.xml를 읽을 수 있도록 지정(12행)
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/*
	 @GetMapping("/.do") public ModelAndView getFirst() { return new
	 ModelAndView("redirect:gb2_list.do"); }
	 */
	
	@GetMapping("gb2_list.do")
	public ModelAndView getGuestBook2List() {
		ModelAndView mv = new ModelAndView("list");
		List<VO> list = guestBook2Service.getGuestBook2List();
		if (list != null) {
			mv.addObject("list", list);
			return mv;
		}
		return new ModelAndView("error");
	}
	
	@GetMapping("gb2_write.do")
	public ModelAndView getGuestBook2Write() {
		return new ModelAndView("write");
	}
	
	@PostMapping("gb2_write_ok.do")
	public ModelAndView getGuestBook2WriteOk(VO vo, HttpServletRequest request) {
		try {
			ModelAndView mv = new ModelAndView("redirect:gb2_list.do");
			String path = request.getSession().getServletContext().getRealPath("/resources/upload");
			
			// 넘어온 파일의 정보 중 파일의 이름은 f_name에 넣어줘야 DB에 저장할 수 있다.
			MultipartFile file = vo.getFile();
			if (file.isEmpty()) {
				vo.setF_name("");
			} else {
				// 파라미터로 받은 file을 이용해서 DB에 저장할 f_name을 채워주자
				// 그러나 같은 이름의 파일이 있으면 업로드가 안되므로
				// 파일이름을 UUID를 이용해서 변경 후 DB에 저장하자.
				UUID uuid = UUID.randomUUID();
				String f_name = uuid.toString()+"_"+file.getOriginalFilename();
				vo.setF_name(f_name);
				
				// 이미지 저장
				byte[] in = vo.getFile().getBytes();
				File out = new File(path, f_name);
				FileCopyUtils.copy(in, out);
			}
			
			// 패스워드 암호화
			String pwd = passwordEncoder.encode(vo.getPwd());
			vo.setPwd(pwd);
			
			// DB 저장
			int result = guestBook2Service.getGuestBook2Insert(vo);
			if (result > 0) {
				return mv;
			} 
			return new ModelAndView("error");
		} catch (Exception e) {
			System.out.println(e);
		}
		return new ModelAndView("error");
	}
	
	@GetMapping("gb2_detail.do")
	public ModelAndView getGuestBook2Detail(String idx) {
		ModelAndView mv = new ModelAndView("onelist");
		VO vo = guestBook2Service.getGuestBook2Detail(idx);
		if (vo != null) {
			mv.addObject("vo", vo);
			return mv;
		}
		return new ModelAndView("error");
	}
	
	@GetMapping("guestbook2_down.do")
	public void getGuestBook2Down(HttpServletRequest request, HttpServletResponse response) {
		try {
			String f_name = request.getParameter("f_name");
			String path = request.getSession().getServletContext().getRealPath("/resources/upload/"+f_name);
			
			// 한글처리
			String r_path = URLEncoder.encode(path, "UTF-8");
			
			// 브라우저 설정
			response.setContentType("application/x-msdownload");
			response.setHeader("Content-Disposition", "attachment; filename="+r_path);
			
			File file = new File(new String(path.getBytes(), "utf-8"));
			FileInputStream in = new FileInputStream(file);
			OutputStream out = response.getOutputStream();
			FileCopyUtils.copy(in, out);
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	@PostMapping("gb2_delete.do")
	public ModelAndView getGuestBook2Delete(@ModelAttribute("vo2")VO vo) {
		return new ModelAndView("delete");
	}
	
	@PostMapping("gb2_delete_ok.do")
	public ModelAndView getGuestBook2DeleteOk(VO vo) {
		ModelAndView mv = new ModelAndView();
		// 비밀번호가 맞는지 틀린지 검사하자. (DB에 있는 비밀번호가 암호로 되어있다.)
		
		// jsp에 입력한 pwd
		String cpwd = vo.getPwd();
		
		VO vo2 = guestBook2Service.getGuestBook2Detail(vo.getIdx());
		// DB에서 가지고 온 암호화된 pwd
		String dpwd = vo2.getPwd();
		
		// passwordEncoder.matches(암호화 되지 않은 것, 암호화 된 것)
		// 일치하면 true, 아니면 false
		if (! passwordEncoder.matches(cpwd, dpwd)) {
			mv.setViewName("delete");
			mv.addObject("pwdchk", "fail");
			mv.addObject("vo2", vo);
			return mv;
		} else {
			int result = guestBook2Service.getGuestBook2Delete(vo.getIdx());
			if (result > 0) {
				mv.setViewName("redirect:gb2_list.do");
				return mv;
			}
		}
		return new ModelAndView("error");
	}
	
	@PostMapping("gb2_update.do")
	public ModelAndView getGuestBook2Update(String idx) {
		ModelAndView mv = new ModelAndView("update");
		VO vo = guestBook2Service.getGuestBook2Detail(idx);
		if (vo != null ) {
			mv.addObject("vo", vo);
			return mv;
		}
		return new ModelAndView("error");
	}
	
	@PostMapping("gb2_update_ok.do")
	public ModelAndView getGuestBook2UpdateOk(VO vo, HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		
		String cpwd = vo.getPwd();
		
		VO vo2 = guestBook2Service.getGuestBook2Detail(vo.getIdx());
		
		String dpwd = vo2.getPwd();
		
		if (! passwordEncoder.matches(cpwd, dpwd)) {
			mv.setViewName("update");
			mv.addObject("pwdchk", "fail");
			mv.addObject("vo", vo2);
			return mv;
		} else {
			try {
				String path = request.getSession().getServletContext().getRealPath("/resources/upload");
				MultipartFile file = vo.getFile();
				String old_f_name = vo.getOld_f_name();
				if (file.isEmpty()) {
					vo.setF_name(old_f_name);
				} else {
					UUID uuid = UUID.randomUUID();
					String f_name = uuid.toString()+"_"+file.getOriginalFilename();
					vo.setF_name(f_name);
					
					// 이미지 복사붙이기
					byte[] in = file.getBytes();
					File out = new File(path, f_name);
					FileCopyUtils.copy(in, out);
				}
				int result = guestBook2Service.getGuestBook2Update(vo);
				if (result > 0) {
					mv.setViewName("redirect:gb2_detail.do?idx="+vo.getIdx());
					return mv;
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return new ModelAndView("error");
	}
	
}
