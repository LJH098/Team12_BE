package pickup_shuttle.pickup.domain.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pickup_shuttle.pickup._core.utils.ApiUtils;
import pickup_shuttle.pickup.config.Login;
import pickup_shuttle.pickup.domain.oauth2.CustomOauth2User;
import pickup_shuttle.pickup.domain.refreshToken.dto.response.AccessTokenRpDTO;
import pickup_shuttle.pickup.domain.user.dto.request.UserModifyRqDTO;
import pickup_shuttle.pickup.domain.user.dto.request.SignUpRqDTO;
import pickup_shuttle.pickup.domain.user.dto.request.UserUploadImageRqDTO;
import pickup_shuttle.pickup.domain.user.dto.response.ModifyUserRpDTO;
import pickup_shuttle.pickup.domain.user.dto.response.UserGetImageUrlRpDTO;
import pickup_shuttle.pickup.security.service.JwtService;


@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    // 은행명, 계좌번호 입력 창으로 이동
    @GetMapping("/users/register/input")
    public ModelAndView userInfoInput() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("registerInput");

        return modelAndView;
    }

    // 에러창
    @GetMapping("/errorPage")
    public ModelAndView error() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorPage");

        return modelAndView;
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(SignUpRqDTO requestDTO, @AuthenticationPrincipal CustomOauth2User customOauth2User, Errors errors){
        userService.signup(requestDTO, customOauth2User);

        return ResponseEntity.ok(ApiUtils.success("처리에 성공하였습니다." + "은행이름: " + requestDTO.bankName() + "  계좌번호: " + requestDTO.accountNum()));
    }

    @GetMapping("/login/callback")
    public ResponseEntity<?> callBack(Authentication authentication){
        CustomOauth2User customOauth2User = (CustomOauth2User) authentication.getPrincipal();
        String userPK = Long.toString(userService.userPK(customOauth2User));
        if(customOauth2User != null){
            String token = jwtService.createAccessToken(userPK);
            return ResponseEntity.ok().body(ApiUtils.success(AccessTokenRpDTO.builder().AccessToken(token).build()));
        } else {
            return ResponseEntity.badRequest().body(ApiUtils.error("인증에 실패하였습니다.", HttpStatus.UNAUTHORIZED));
        }
    }

    // 유저 인증 상태 (인증/미인증/인증 진행 중) 반환
    @GetMapping("/mypage/auth")
    public ResponseEntity<?> userAuthStatus(@Login Long userId){
        String status = userService.userAuthStatus(userId);
        return ResponseEntity.ok(ApiUtils.success(status));
    }

    @GetMapping("/modify")
    public ModelAndView userModify() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("userModify");

        return modelAndView;
    }

    @PutMapping("/mypage/modify")
    public ResponseEntity<?> userModify(@Login Long id ,@RequestBody @Valid UserModifyRqDTO userModifyRqDTO){
        boolean authUser = userService.modifyUser(userModifyRqDTO, id);
        if(authUser){
            return ResponseEntity.ok().body(ApiUtils.success(ModifyUserRpDTO.builder().response("회원 수정이 완료되었습니다").build()));
        } else {
            return ResponseEntity.ok().body(ApiUtils.error("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED));
        }
    }
    // 이미지 업로드
    @PutMapping("/mypage/image/url")
    public ResponseEntity<?> uploadImage(@ModelAttribute @Valid UserUploadImageRqDTO requestDTO,
                                       @Login Long userId) {
        userService.uploadImage(requestDTO.image(), userId);
        return ResponseEntity.ok(ApiUtils.success("이미지 url 저장이 완료되었습니다"));
    }
    // presigendUrl(GET) 발급
    @GetMapping("/mypage/image/url")
    public ResponseEntity<?> getImageUrl(@Login Long userId) {
        UserGetImageUrlRpDTO responseDTO = userService.getImageUrl(userId);
        return ResponseEntity.ok(ApiUtils.success(responseDTO));
    }
}