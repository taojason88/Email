package net.risesoft.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.james.service.JamesUserService;
import net.risesoft.model.platform.Person;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.configuration.Y9Properties;

@RestController(value = "standardSyncController")
@RequestMapping(value = "/api/standard")
public class SyncController {

    @Autowired
    Y9Properties y9config;
    @Autowired
    JamesUserService jamesUserService;
    @Autowired
    private PersonApi personApi;

    @RequestMapping(value = "/syncUsers")
    public Y9Result<String> syncUsers() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Person> personList = personApi.listAllPersons(tenantId).getData();
        int allcount = personList.size(), disabledcount = 0, deletedcount = 0, successcount = 0, errorcount = 0;
        String errorNames = "";
        for (Person person : personList) {
            if (person.getDisabled()) {
                disabledcount++;
                continue;
            }
            try {
                jamesUserService.add(person.getId(), person.getLoginName());
                successcount++;
            } catch (Exception e) {
                errorcount++;
                if (StringUtils.isBlank(errorNames)) {
                    errorNames = errorcount + "、" + person.getName() + "(" + person.getLoginName() + ")";
                } else {
                    errorNames += "</br>" + errorcount + "、" + person.getName() + "(" + person.getLoginName() + ")";
                }
                e.printStackTrace();
            }
        }
        String msg = "总共：" + allcount + "人" + "</br>" + "禁用：" + disabledcount + "人" + "</br>" + "删除：" + deletedcount
            + "人" + "</br>" + "成功：" + successcount + "人" + "</br>" + "失败：" + errorcount + "人" + "</br>" + "失败人员列表："
            + errorNames;
        return Y9Result.success(msg);
    }
}