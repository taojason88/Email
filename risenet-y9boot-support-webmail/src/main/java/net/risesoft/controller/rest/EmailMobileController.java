package net.risesoft.controller.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.risesoft.james.entity.JamesAddressBook;
import net.risesoft.james.service.JamesAddressBookService;
import net.risesoft.support.EmailThreadLocalHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.org.OrganizationApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.controller.dto.EmailAttachmentDTO;
import net.risesoft.controller.dto.EmailDTO;
import net.risesoft.controller.dto.EmailDetailDTO;
import net.risesoft.controller.dto.EmailFolderDTO;
import net.risesoft.controller.dto.EmailListDTO;
import net.risesoft.controller.dto.EmailSearchDTO;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.enums.platform.OrgTreeTypeEnum;
import net.risesoft.james.service.JamesUserService;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Organization;
import net.risesoft.model.platform.Person;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.EmailAttachmentService;
import net.risesoft.service.EmailFolderService;
import net.risesoft.service.EmailService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 电子邮件接口
 */
@RestController
@RequestMapping("/mobile")
public class EmailMobileController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private PersonApi personApi;
    @Autowired
    private EmailAttachmentService emailAttachmentService;
    @Autowired
    private EmailFolderService emailFolderService;
    @Autowired
    private OrgUnitApi orgUnitApi;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private JamesUserService jamesUserService;

    @Autowired
    private JamesAddressBookService jamesAddressBookService;

    /**
     * 删除邮件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uids 邮件 uid
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link Object}>
     * @throws MessagingException
     */
    @DeleteMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> delete(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(value = "uids") long[] uids, String folder)
        throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.delete(folder, uids);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 永久删除邮件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uids 邮件 uid
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link Object}>
     * @throws MessagingException
     */
    @DeleteMapping(value = "/permanently", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> deletePermanently(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(value = "uids") long[] uids, String folder)
        throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.deletePermanently(folder, uids);
        return Y9Result.successMsg("永久删除成功");
    }

    /**
     * 邮件详情
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param uid 邮件 uid
     * @return {@link Y9Result}<{@link EmailDetailDTO}>
     * @throws Exception
     */
    @GetMapping(value = "/{folder}/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailDetailDTO> detail(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @PathVariable String folder, @PathVariable long uid)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        EmailDetailDTO email = emailService.detail(folder, uid);
        return Y9Result.success(email);
    }

    /**
     * 导出邮件 eml
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param uid 邮件 uid
     * @param response 响应
     * @param request 请求
     * @throws MessagingException
     * @throws IOException
     */
    @GetMapping(value = "/exportEml", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportEml(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder, int uid, HttpServletResponse response,
        HttpServletRequest request) throws MessagingException, IOException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.exportEml(folder, uid, response, request);
    }

    /**
     * 邮件标星
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uids 邮件 uid
     * @param folder 文件夹
     * @param flagged 是否标星
     * @return {@link Y9Result}<{@link Object}>
     * @throws Exception
     */
    @PostMapping(value = "/flag", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> flag(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(value = "uids") long[] uids,
        @RequestParam String folder, boolean flagged) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.flag(folder, uids, flagged);
        return Y9Result.successMsg("标星成功");
    }

    /**
     * 转发邮件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uid 消息 uid
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link EmailDTO}>
     * @throws Exception
     */
    @GetMapping(value = "/forward/{folder}/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailDTO> forward(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @PathVariable String folder, @PathVariable long uid)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(emailService.forward(folder, uid));
    }

    /**
     * 根据文件夹分页查找邮件列表
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param page 页数
     * @param size 每页数量
     * @param folder 文件夹
     * @return {@link Y9Page}<{@link EmailListDTO}>
     * @throws IOException
     * @throws MessagingException
     */
    @GetMapping(value = "/list")
    public Y9Page<EmailListDTO> list(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, int page, @RequestParam int size,
        @RequestParam(required = false) String folder) throws IOException, MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return emailService.listByFolder(folder, page, size);
    }

    /**
     * 移动邮件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uids 邮件 uid
     * @param originFolder 原始文件夹
     * @param toFolder 移动至文件夹
     * @return {@link Y9Result}<{@link Object}>
     * @throws MessagingException
     */
    @PostMapping(value = "/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> move(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(value = "uids") long[] uids,
        String originFolder, String toFolder) throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.move(uids, originFolder, toFolder);
        return Y9Result.successMsg("移动成功");
    }

    /**
     * 标记为已读或未读
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uids 邮件 uid
     * @param folder 文件夹
     * @param isRead 是否已读
     * @return {@link Y9Result}<{@link Object}>
     * @throws Exception
     */
    @PostMapping(value = "/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> read(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(value = "uids") long[] uids,
        @RequestParam String folder, @RequestParam Boolean isRead) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.read(folder, uids, isRead);
        return Y9Result.success();
    }

    /**
     * 回复
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uid 邮件 uid
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link EmailDTO}>
     * @throws Exception
     */
    @GetMapping(value = "/reply/{folder}/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailDTO> reply(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @PathVariable String folder, @PathVariable Long uid)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(emailService.reply(folder, uid));
    }

    /**
     * 快速回复
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param uid uid
     * @return {@link Y9Result}<{@link EmailDTO}>
     * @throws Exception 异常
     */
    @PostMapping(value = "/quickReply/{folder}/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailDTO> quickReply(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @PathVariable String folder, @PathVariable Long uid,
        @RequestParam String richText) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.quickReply(folder, uid, richText);
        return Y9Result.successMsg("快捷回复成功");
    }

    /**
     * 回复不包括自己和密送的其他所有收件人
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param uid 邮件 uid
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link EmailDTO}>
     * @throws Exception
     */
    @GetMapping(value = "/replyAll/{folder}/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailDTO> replyAll(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @PathVariable String folder, @PathVariable Long uid)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(emailService.replyAll(folder, uid));
    }

    /**
     * 保存邮件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param email 邮件
     * @return {@link Y9Result}<{@link String}>
     * @throws Exception
     */
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<String> save(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, EmailDTO email) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        String messageId = emailService.save(email);
        return Y9Result.success(messageId, "保存成功");
    }

    /**
     * 搜索邮件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param searchDTO 搜索dto
     * @return {@link List}<{@link EmailListDTO}>
     * @throws MessagingException
     * @throws IOException
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Page<EmailListDTO> search(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, EmailSearchDTO searchDTO)
        throws MessagingException, IOException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return emailService.search(searchDTO, searchDTO.getPage(), searchDTO.getSize());
    }

    /**
     * 发送邮件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param messageId 消息id
     * @return {@link Y9Result}<{@link Object}>
     * @throws Exception
     */
    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> send(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String messageId) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailService.send(messageId);
        return Y9Result.successMsg("发送成功");
    }

    /**
     * 待办数量列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @return {@link Y9Result}<{@link Object}>
     * @throws MessagingException
     */
    @GetMapping(value = "/todoList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> todoList(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        String personId = Y9LoginUserHolder.getUserInfo().getPersonId();
        Map<String, Object> todoList = emailService.getTodoCount(personId);
        return Y9Result.success(todoList);
    }

    /**
     * 上传附件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param messageId 消息id
     * @param file 文件
     * @return
     */
    @PostMapping(value = "/addAttachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<EmailAttachmentDTO> addAttachment(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder, String messageId, MultipartFile file)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        EmailAttachmentDTO emailAttachmentDTO = emailAttachmentService.addAttachment(folder, messageId, file);
        return Y9Result.success(emailAttachmentDTO);
    }

    /**
     * 附件下载
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param messageId 消息id
     * @param fileName 文件名
     * @param response 响应
     * @param request 请求
     */
    @RequestMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public void download(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder, String messageId, String fileName,
        HttpServletResponse response, HttpServletRequest request) {
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Person person = personApi.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
            EmailThreadLocalHolder.setEmailAddress(emailAddress);
            emailAttachmentService.download(folder, messageId, fileName, response, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量下载文件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param messageId 消息id
     * @param request 请求
     * @param response 响应
     */
    @RequestMapping(value = "/batchDownload", produces = MediaType.APPLICATION_JSON_VALUE)
    public void batchDownload(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder, String messageId,
        HttpServletRequest request, HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailAttachmentService.batchDownload(folder, messageId, request, response);
    }

    /**
     * 删除附件
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @param messageId 消息id
     * @param fileName 文件名称
     * @return {@link Y9Result}<{@link Object}>
     * @throws Exception 异常
     */
    @DeleteMapping(value = "/deleteAttachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> deleteAttachment(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder, String messageId, String fileName)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailAttachmentService.removeAttachment(folder, messageId, fileName);
        return Y9Result.success();
    }

    /**
     * 新增或修改文件夹
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param originFolderName 原始文件夹名称 （不为空时代表修改）
     * @param newFolderName 新文件夹名称
     * @return {@link Y9Result}<{@link Object}>
     */
    @PostMapping(value = "/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> save(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String originFolderName, String newFolderName) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailFolderService.save(originFolderName, newFolderName);
        return Y9Result.success();
    }

    /**
     * 删除文件夹
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folder 文件夹
     * @return {@link Y9Result}<{@link Object}>
     */
    @DeleteMapping(value = "/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> delete(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String folder) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        emailFolderService.delete(folder);
        return Y9Result.success();
    }

    /**
     * 自定义文件夹列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    @GetMapping(value = "/folder/customList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<List<EmailFolderDTO>> customList(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        List<EmailFolderDTO> emailFolderList = emailFolderService.list();
        return Y9Result.success(emailFolderList);
    }

    /**
     * 所有文件夹列表，包括默认文件夹和自定义文件夹列表
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return {@link Y9Result}<{@link Map}<{@link String}, {@link Object}>>
     * @throws MessagingException 通讯异常
     */
    @GetMapping(value = "/allList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Map<String, Object>> list(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) throws MessagingException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        List<EmailFolderDTO> customFolderList = emailFolderService.list();
        List<EmailFolderDTO> defaultFolderList = emailFolderService.getDefaultFolderList();
        Map<String, Object> data = new HashMap<>();
        data.put("customFolderList", customFolderList);
        data.put("defaultFolderList", defaultFolderList);
        return Y9Result.success(data);
    }

    /**
     * 获取组织树
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param id id
     * @param treeType 树类型
     * @param name 名字
     * @return {@link Y9Result}<{@link List}<{@link OrgUnit}>>
     */
    @RequestMapping(value = "/getOrgTree", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<List<OrgUnit>> getOrgTree(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, @RequestParam(required = false) String id,
        @RequestParam(required = false) OrgTreeTypeEnum treeType, @RequestParam(required = false) String name) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        if (StringUtils.isBlank(id)) {
            List<Organization> organizationList = organizationApi.listAllOrganizations(tenantId).getData();
            if (organizationList != null && organizationList.size() > 0) {
                id = organizationList.get(0).getId();
            }
        }
        List<OrgUnit> orgUnitList;
        if (StringUtils.isNotBlank(name)) {
            orgUnitList = orgUnitApi.treeSearch(tenantId, name, treeType).getData();
            orgUnitList = mapEmailAddress(orgUnitList);
        } else {
            orgUnitList = orgUnitApi.getSubTree(tenantId, id, treeType).getData();
            orgUnitList = mapEmailAddress(orgUnitList);
        }
        return Y9Result.success(orgUnitList);
    }

    private List<OrgUnit> mapEmailAddress(List<OrgUnit> orgUnitList) {
        orgUnitList = orgUnitList.stream().peek(org -> {
            if (OrgTypeEnum.PERSON.equals(org.getOrgType())) {
                Person person = (Person)org;
                String email = jamesUserService.getEmailAddressByPersonId(org.getId());
                if (StringUtils.isEmpty(email) && StringUtils.isEmpty(person.getEmail())) {
                    email = "未注册邮箱";
                } else {
                    if (!StringUtils.isEmpty(email)) {
                        person.setEmail(email);
                    }
                    if (!StringUtils.isEmpty(person.getEmail())) {
                        email = person.getEmail();
                    }
                }
                person.setName(org.getName() + "(" + email + ")");
            }
        }).collect(Collectors.toList());
        return orgUnitList;
    }

    @GetMapping(value = "/getPersonData", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Map<String, String>> getPersonData(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) throws MessagingException {
        Person person = personApi.getPerson(tenantId, userId).getData();
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        Map<String, String> map = new HashMap<>();
        map.put("name", person.getName());
        map.put("emailAddress", emailAddress);
        map.put("avator", person.getAvator());
        return Y9Result.success(map);
    }

    @GetMapping(value = "/contact", produces = MediaType.APPLICATION_JSON_VALUE)
    public Y9Result<Object> contact(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) throws MessagingException, IOException {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(emailService.contactPerson());
    }

    /**
     * 个人通讯录保存/修改
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param jamesAddressBook
     * @return
     */
    @PostMapping(value = "/addressBookSave")
    public Y9Result<Object> addressBookSave(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, JamesAddressBook jamesAddressBook) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(jamesAddressBookService.saveOrUpdate(jamesAddressBook), "保存成功");
    }

    /**
     * 个人通讯录查询
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param search 邮件地址/姓名
     * @return
     */
    @GetMapping(value = "/addressBooksearch")
    public Y9Result<Object> addressBooksearch(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String search) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(jamesAddressBookService.findSearch(search));
    }

    /**
     * 通讯录详情
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param id 通讯录唯一id
     * @return
     */
    @GetMapping(value = "/addressBook")
    public Y9Result<Object> addressBook(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String email = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(email);
        return Y9Result.success(jamesAddressBookService.findOne(id));
    }

    /**
     * 获取个人通讯录列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    @GetMapping(value = "/addressBookList")
    public Y9Result<Object> addressBookList(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String emailAddress = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(emailAddress);
        return Y9Result.success(jamesAddressBookService.findAll());
    }

    /**
     * 删除个人通讯录
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param id 通讯录唯一id
     * @return
     */
    @DeleteMapping(value = "/addressBookDelete")
    public Y9Result<Object> addressBookDelete(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String email = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(email);
        jamesAddressBookService.delete(id);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 邮件地址关联
     * 
     * @param tenantId 租户id
     * @param userId 用户id search 邮件地址/姓名
     * @return
     */
    @GetMapping(value = "/addressRelevancy")
    public Y9Result<Object> addressRelevancy(@RequestHeader(value = "auth-tenantId") String tenantId,
        @RequestHeader(value = "auth-userId") String userId, String search) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personApi.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        String email = jamesUserService.getEmailAddressByPersonId(userId);
        EmailThreadLocalHolder.setEmailAddress(email);
        return Y9Result.success(emailService.addressRelevancy(search));
    }

}