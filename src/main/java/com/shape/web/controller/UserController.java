package com.shape.web.controller;

import com.shape.web.entity.FileDB;
import com.shape.web.entity.Role;
import com.shape.web.entity.User;
import com.shape.web.repository.FileDBRepository;
import com.shape.web.repository.UserRepository;
import com.shape.web.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by seongahjo on 2016. 2. 7..
 */

/**
 * Handles requests for the User .
 */
@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    FileDBRepository fileDBRepository;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public ResponseEntity register(@ModelAttribute("tempUser") @Valid User tempUser, BindingResult result, @RequestParam("file") MultipartFile file) {
        if (!result.hasErrors()) {
            User user = userRepository.findById(tempUser.getId());
            if (user == null)
                user = new User();

            user.setId(tempUser.getId());
            user.setName(tempUser.getName());
            if (!tempUser.getPw().equals("")) // 비밀번호란이 공란이 아닐경우
                user.setPw(tempUser.getPw());
            user.setRole(new Role("user"));
            logger.info("Register start");
            try {
                String filePath = "img";
                String originalFileName = file.getOriginalFilename(); // 파일 이름
                String originalFileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 파일 확장자
                String storedFileName = CommonUtils.getRandomString() + originalFileExtension; //암호화된 고유한 파일 이름
                FileDB filedb = new FileDB(storedFileName, originalFileName, filePath, "img", null);
                File folder = new File(filePath); // 폴더
                if (!folder.exists()) // 폴더 존재하지 않을 경우 만듬
                    folder.mkdirs();
                File transFile = new File(filePath + "/" + originalFileName); // 전송된 파일
                logger.info("FILE NAME = " + file.getOriginalFilename());
                file.transferTo(transFile);
                fileDBRepository.saveAndFlush(filedb); // 파일 내용을 디비에 저장
                user.setImg("loadImg?name=" + storedFileName);

                filedb.setUser(user);
                userRepository.saveAndFlush(user);
                logger.info("Register Success " + user.getName());
            } catch (IOException ioe) {

            } catch (StringIndexOutOfBoundsException e) {
                if (user.getImg() == null)
                    user.setImg("img/default.jpg");
                //이미지를 선택하지 않았을 경우 이미지를 제외한 정보만 수정
                userRepository.saveAndFlush(user);
                logger.info("Register Success " + user.getName());
            } finally {
                return new ResponseEntity(HttpStatus.CREATED);
            }
        } // hasErrors end
        else {
            logger.info("Register Error");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


}
