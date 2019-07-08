package com.faw_vk.qak.mgmt.service.issue.impl;

import com.efast.cafe.framework.exception.ServiceException;
import com.efast.cafe.framework.util.SpringWebUtils;
import com.efast.cafe.portal.entity.company.PortalCompanyOrg;
import com.efast.cafe.util.date.DateUtils;
import com.faw_vk.qak.mgmt.dao.report.QualityIssueReportDao;
import com.faw_vk.qak.mgmt.entity.QualityIssue;
import com.faw_vk.qak.mgmt.entity.QualityReport;
import com.faw_vk.qak.mgmt.service.base.BaseDataService;
import com.faw_vk.qak.mgmt.service.report.IQualityIssueReportService;
import com.faw_vk.qak.mgmt.util.ConstantsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
public class QualityIssueReportServiceImpl implements IQualityIssueReportService {

    private static Logger logger = LoggerFactory.getLogger(QualityIssueReportServiceImpl.class);

    @Autowired
    private QualityIssueReportDao qualityIssueReportDao;

    @Autowired
    private BaseDataService baseDataService;

    /**
     * 根据质量问题单类型（基础类/过程类）
     * 返回专业组（撤回、编辑中、经验总结、质量要求）
     *
     * @param issueType
     * @return
     */
    public List<QualityReport> generateQualityIssueReport(String issueType) {

        List<QualityReport> result = new ArrayList<>();
        //撤回
        QualityIssue issue = new QualityIssue();
        List<String> status = new ArrayList<String>();
        issue.setRequireType(issueType);
        status.add("withdrawn");
        status.add("r-withdraw");
        issue.setReportIssueStatus(status);
        // 过程类 撤回 专业组分类
        List<QualityIssue> withrawIssues = qualityIssueReportDao.queryQualityByTypeAndStatus(issue);
        if (withrawIssues != null && withrawIssues.size() > 0) {
            for (QualityIssue withraw : withrawIssues) {
                QualityReport report = new QualityReport();
                report.setProGroup(withraw.getMajorGroup());
                report.setWithdrawCount(withraw.getCount());

                result.add(report);
            }
        }

        //编辑中
        status.clear();
        status.add("assigned");
        status.add("withdraw-recheck");
        issue.setReportIssueStatus(status);
        issue.setFormType("issue");
        // 基础类 状态 专业组分类
        List<QualityIssue> editIssues = qualityIssueReportDao.queryQualityByTypeAndStatus(issue);

        status.clear();
        status.add("tobecheck");
        status.add("rejected");
        issue.setReportIssueStatus(status);
        issue.setFormType("experience");
        // 基础类 状态 专业组分类
        List<QualityIssue> editExperience = qualityIssueReportDao.queryQualityByTypeAndStatus(issue);

        // 合并
        if (editExperience != null && editExperience.size() > 0) {
            if (editIssues == null || editIssues.size() <= 0) {
                editIssues = editExperience;
            } else {
                Iterator<QualityIssue> editIt = editExperience.iterator();
                while (editIt.hasNext()) {
                    QualityIssue editNext = editIt.next();
                    for (QualityIssue is : editIssues) {
                        if (editNext.getMajorGroup().equals(is.getMajorGroup()) &&
                                editNext.getCount() != null) {
                            is.setCount(editNext.getCount() + is.getCount());
                            editIt.remove();
                        }
                    }
                }
                if (editExperience.size() > 0) {
                    editIssues.addAll(editExperience);
                }
            }
        }

        // 整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个组中
        if (editIssues != null && editIssues.size() > 0) {
            Iterator<QualityIssue> editrawIt = editIssues.iterator();
            while (editrawIt.hasNext()) {
                QualityIssue editraw = editrawIt.next();
                for (QualityReport report : result) {
                    if (editraw.getMajorGroup().equals(report.getProGroup())) {
                        report.setEditCount(editraw.getCount());
                        editrawIt.remove();
                    }
                }
            }
            if (editIssues.size() > 0) {
                for (QualityIssue edit : editIssues) {
                    QualityReport report = new QualityReport();
                    report.setProGroup(edit.getMajorGroup());
                    report.setEditCount(edit.getCount());

                    result.add(report);
                }
            }
        }

        //经验总结
        status.clear();
        status.add("experience");
        status.add("withdraw-recheck");
        issue.setFormType("experience");
        issue.setReportIssueStatus(status);
        //基础类 状态 专业组分类
        List<QualityIssue> experices = qualityIssueReportDao.queryQualityByTypeAndStatus(issue);
        //整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个组中
        if (experices != null && experices.size() > 0) {
            Iterator<QualityIssue> expericesIt = experices.iterator();
            while (expericesIt.hasNext()) {
                QualityIssue exp = expericesIt.next();
                for (QualityReport report : result) {
                    if (exp.getMajorGroup().equals(report.getProGroup())) {
                        report.setExpressionCount(exp.getCount());
                        expericesIt.remove();
                    }
                }
            }
            if (experices.size() > 0) {
                for (QualityIssue exp : experices) {
                    QualityReport report = new QualityReport();
                    report.setProGroup(exp.getMajorGroup());
                    report.setExpressionCount(exp.getCount());

                    result.add(report);
                }
            }
        }

        //质量要求
        status.clear();
        status.add("require");
        status.add("r-withdraw-recheck");
        //过程类
        if (ConstantsUtil.QAKRequireType.PROCESS.equals(issueType)) {
            status.add("r-to-contract");
        }
        //基础类
        if (ConstantsUtil.QAKRequireType.BASE.equals(issueType)) {
            status.add("r-to-germany");
            status.add("r-ae-basic");
            status.add("r-to-basic");
            status.add("r-ae-te");
            status.add("r-germany-reject");
        }

        issue.setFormType("require");
        issue.setReportIssueStatus(status);
        // 基础类 状态 专业组分类
        List<QualityIssue> requires = qualityIssueReportDao.queryQualityByTypeAndStatus(issue);
        // 整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个组中
        if (requires != null && requires.size() > 0) {
            Iterator<QualityIssue> requiresIt = requires.iterator();
            while (requiresIt.hasNext()) {
                QualityIssue req = requiresIt.next();
                for (QualityReport report : result) {
                    if (req.getMajorGroup().equals(report.getProGroup())) {
                        report.setRequireCount(req.getCount());
                        requiresIt.remove();
                    }
                }
            }
            if (requires.size() > 0) {
                for (QualityIssue req : requires) {
                    QualityReport report = new QualityReport();
                    report.setProGroup(req.getMajorGroup());
                    report.setRequireCount(req.getCount());

                    result.add(report);
                }
            }
        }
        return result;
    }

    /**
     * 质量要求 按部门分组
     */
    public List<QualityReport> generateQualityIssueDepReport() {

        List<QualityReport> result = new ArrayList<>();

        //撤回
        QualityIssue issue = new QualityIssue();
        List<String> status = new ArrayList<String>();
//        issue.setRequireType("processRequire");
        status.add("withdrawn");
        status.add("r-withdraw");
        issue.setReportIssueStatus(status);
        // 过程类 状态 部门分组
        List<QualityIssue> withrawIssuesBefore = qualityIssueReportDao.queryProcessQualityByTypeAndStatus(issue);
        // 查找一级部门
        List<QualityIssue> withrawIssues = getFirstLevelTreeNode(withrawIssuesBefore);

        if (withrawIssues != null && withrawIssues.size() > 0) {
            for (QualityIssue withraw : withrawIssues) {
                QualityReport report = new QualityReport();
                report.setDepartment(withraw.getPresUserLocal());
                report.setWithdrawCount(withraw.getCount());

                result.add(report);
            }
        }

        //编辑中
        status.clear();
        status.add("assigned");
        status.add("withdraw-recheck");
        issue.setReportIssueStatus(status);
        issue.setFormType("issue");
        //过程类 状态 部门分组
        List<QualityIssue> editIssuesBefore = qualityIssueReportDao.queryProcessQualityByTypeAndStatus(issue);
        // 查找一级部门
        List<QualityIssue> editIssues = getFirstLevelTreeNode(editIssuesBefore);
        status.clear();
        status.add("tobecheck");
        status.add("rejected");
        issue.setReportIssueStatus(status);
        issue.setFormType("experience");
        //过程类 状态 部门分组
        List<QualityIssue> editExperienceBefore = qualityIssueReportDao.queryProcessQualityByTypeAndStatus(issue);
        // 查找一级部门
        List<QualityIssue> editExperience = getFirstLevelTreeNode(editExperienceBefore);

        // 按部门整合
        if (editExperience != null && editExperience.size() > 0) {
            if (editIssues == null || editIssues.size() <= 0) {
                editIssues = editExperience;
            } else {
                Iterator<QualityIssue> editIt = editExperience.iterator();
                while (editIt.hasNext()) {
                    QualityIssue editNext = editIt.next();
                    for (QualityIssue is : editIssues) {
                        if (editNext.getPresUserLocal().equals(is.getPresUserLocal()) &&
                                editNext.getCount() != null) {
                            is.setCount(editNext.getCount() + is.getCount());
                            editIt.remove();
                        }
                    }
                }
                if (editExperience.size() > 0) {
                    editIssues.addAll(editExperience);
                }
            }
        }
        // 整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个部门中
        if (editIssues != null && editIssues.size() > 0) {
            Iterator<QualityIssue> editrawIt = editIssues.iterator();
            while (editrawIt.hasNext()) {
                QualityIssue editraw = editrawIt.next();
                for (QualityReport report : result) {
                    if (editraw.getPresUserLocal().equals(report.getDepartment())) {
                        report.setEditCount(editraw.getCount());
                        editrawIt.remove();
                    }
                }
            }
            if (editIssues.size() > 0) {
                for (QualityIssue edit : editIssues) {
                    QualityReport report = new QualityReport();
                    report.setDepartment(edit.getPresUserLocal());
                    report.setEditCount(edit.getCount());

                    result.add(report);
                }
            }
        }

        //经验总结
        status.clear();
        status.add("experience");
        status.add("withdraw-recheck");
        issue.setFormType("experience");
        issue.setReportIssueStatus(status);
        //过程类 状态 部门分组
        List<QualityIssue> expericesBefore = qualityIssueReportDao.queryProcessQualityByTypeAndStatus(issue);
        // 查找一级部门
        List<QualityIssue> experices = getFirstLevelTreeNode(expericesBefore);
        //整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个部门中
        if (experices != null && experices.size() > 0) {
            Iterator<QualityIssue> expericesIt = experices.iterator();
            while (expericesIt.hasNext()) {
                QualityIssue exp = expericesIt.next();
                for (QualityReport report : result) {
                    if (exp.getPresUserLocal().equals(report.getDepartment())) {
                        report.setExpressionCount(exp.getCount());
                        expericesIt.remove();
                    }
                }
            }
            if (experices.size() > 0) {
                for (QualityIssue exp : experices) {
                    QualityReport report = new QualityReport();
                    report.setDepartment(exp.getPresUserLocal());
                    report.setExpressionCount(exp.getCount());

                    result.add(report);
                }
            }
        }

        //质量要求
        status.clear();
        status.add("require");
        status.add("r-withdraw-recheck");
        //过程类
        status.add("r-to-contract");
        issue.setFormType("require");
        issue.setReportIssueStatus(status);
        // 过程类 状态 部门分组
        List<QualityIssue> requiresBefore = qualityIssueReportDao.queryProcessQualityByTypeAndStatus(issue);
        // 查找一级部门
        List<QualityIssue> requires = getFirstLevelTreeNode(requiresBefore);
        // 整合不同数据（撤回、编辑中、经验总结、质量要求）到同一个部门中
        if (requires != null && requires.size() > 0) {
            Iterator<QualityIssue> requiresIt = requires.iterator();
            while (requiresIt.hasNext()) {
                QualityIssue req = requiresIt.next();
                for (QualityReport report : result) {
                    if (req.getPresUserLocal().equals(report.getDepartment())) {
                        report.setRequireCount(req.getCount());
                        requiresIt.remove();
                    }
                }
            }
            if (requires.size() > 0) {
                for (QualityIssue req : requires) {
                    QualityReport report = new QualityReport();
                    report.setDepartment(req.getPresUserLocal());
                    report.setRequireCount(req.getCount());

                    result.add(report);
                }
            }
        }
        return result;
    }

    // 质量要求周报 过程类
    @Override
    public List<QualityReport> qualityIssueWeekReport() {
        List<QualityReport> result = generateQualityIssueReport(ConstantsUtil.QAKRequireType.PROCESS);
        if (result != null && result.size() > 0) {
            //设置时间
            String createDate = DateUtils.dateParseString(new Date());
            //当前时间为一年中第几周
            Integer week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
            for (QualityReport res : result) {
                //过程类
                res.setRequireType(ConstantsUtil.QAKRequireType.PROCESS);
                res.setCreateDate(createDate);
                res.setCreateWeekOfYear(week);
                res.setCreateYearMonth(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                res.setReportType(ConstantsUtil.QAKReportType.BASE_EEEK);
                //count为null时设置为0
                setDefaultCount(res);
            }
        }
        return result;
    }

    // 质量要求 时间
    @Override
    public List<QualityReport> qualityIssueTimeReport() {
        List<QualityReport> result = generateQualityIssueDepReport();
        //设置时间
        setReportTime(result, null, ConstantsUtil.QAKReportType.REPORT_TIME);
        return result;
    }

    // 过程类 质量要求 部门
    @Override
    public List<QualityReport> qualityIssueDepReport() {
        List<QualityReport> result = generateQualityIssueDepReport();
        //设置时间
        setReportTime(result, null, ConstantsUtil.QAKReportType.REPORT_DEPARTMENT);
        return result;
    }

    // 质量要求 专业组
    @Override
    public List<QualityReport> qualityIssueMajorGroupReport() {
        List<QualityReport> result = generateQualityIssueReport(null);
        //设置时间
        setReportTime(result, null, ConstantsUtil.QAKReportType.REPORT_MAJORGROUP);
        return result;
    }

    // 质量要求过程类周报 插入数据库
    @Override
    public void scheduleInsertWeekReport() {
        List<QualityReport> reports = qualityIssueWeekReport();
        insertDataSchedu(reports);
    }

    // 质量要求月报 插入数据库
    @Override
    public void scheduleInsertMonthReport() {
        List<QualityReport> reports = qualityIssueTimeReport();
        insertDataSchedu(reports);
    }

    // 质量要求部门 插入数据库
    @Override
    public void scheduleInsertDepReport() {
        List<QualityReport> reports = qualityIssueDepReport();
        insertDataSchedu(reports);
    }

    // 质量要求专业组 插入数据库
    @Override
    public void scheduleInsertMajReport() {
        List<QualityReport> reports = qualityIssueMajorGroupReport();
        insertDataSchedu(reports);
    }

    // 数据插入数据库
    public void insertDataSchedu(List<QualityReport> reports) {
        try {
            if(reports != null && reports.size() > 0) {
                QualityReport report = new QualityReport();
                QualityReport rep = reports.get(0);
                if(ConstantsUtil.QAKReportType.BASE_EEEK.equals(rep.getReportType())) {
                    report.setCreateWeekOfYear(rep.getCreateWeekOfYear());
                    report.setRequireType(rep.getRequireType());
                    report.setReportType(rep.getReportType());
                    report.setCreateYearMonth(rep.getCreateYearMonth());
                }
                if(ConstantsUtil.QAKReportType.REPORT_TIME.equals(rep.getReportType()) ||
                        ConstantsUtil.QAKReportType.REPORT_DEPARTMENT.equals(rep.getReportType())) {
                    for(QualityReport repo : reports) {
                        String department = repo.getDepartment();
                        if(department.contains("\n")) {
                            department = department.substring(department.indexOf("\n") + 1);
                        }
                        repo.setDepartment(department);
                    }
                    report.setRequireType(rep.getRequireType());
                    report.setCreateYearMonth(rep.getCreateYearMonth());
                    report.setReportType(rep.getReportType());
                }

                if(ConstantsUtil.QAKReportType.REPORT_MAJORGROUP.equals(rep.getReportType())) {
                    report.setRequireType(rep.getRequireType());
                    report.setCreateYearMonth(rep.getCreateYearMonth());
                    report.setReportType(rep.getReportType());
                }
                qualityIssueReportDao.deleteDataSchedu(report);
                qualityIssueReportDao.insertDataSchedu(reports);
            }

        } catch (Exception e) {
            logger.error("定时插入数据异常！", e);
            throw new ServiceException("定时插入数据异常！");
        }
    }

    //查找一级部门
    public List<QualityIssue> getFirstLevelTreeNode(List<QualityIssue> issues) {
        // 查找所有的部门
        List<PortalCompanyOrg> portalCompanyOrgs = baseDataService.queryAllCompanyOrgByCompanyCode(SpringWebUtils.getRequestCompany());
        // 递归找到一级部门
        for (QualityIssue issue : issues) {
            //根据部门名称查找部门信息  P-Q-FS
            //佛山质保部
            String preUserLocal = issue.getPresUserLocal();
            if(preUserLocal.contains("\n")) {
                preUserLocal = preUserLocal.substring(preUserLocal.indexOf("\n") + 1);
            }
            PortalCompanyOrg preOrg = baseDataService.queryOrgInfoByOrgName(preUserLocal);

            // 一级部门
            if (null != preOrg) {
                PortalCompanyOrg companyOrg = queryParentOrg(portalCompanyOrgs, preOrg);
                issue.setPresUserLocal(companyOrg.getOrg_name());
            }
        }

        // 合并相同一级部门订单数量
        Map<String, QualityIssue> result = new HashMap<>();
        List<String> orgs = new ArrayList<>();
        for (QualityIssue issue : issues) {
            if (!orgs.contains(issue.getPresUserLocal())) {
                result.put(issue.getPresUserLocal(), issue);
                orgs.add(issue.getPresUserLocal());
            } else {
                issue.setCount(result.get(issue.getPresUserLocal()).getCount() + issue.getCount());
                result.put(issue.getPresUserLocal(), issue);
            }
        }
        Set<Map.Entry<String, QualityIssue>> entries = result.entrySet();
        Iterator<Map.Entry<String, QualityIssue>> iterator = entries.iterator();
        List<QualityIssue> rs = new ArrayList<>();
        while (iterator.hasNext()) {
            rs.add(iterator.next().getValue());
        }

        return rs;
    }

    //递归查找一级部门
    private PortalCompanyOrg queryParentOrg(List<PortalCompanyOrg> portalCompanyOrgs, PortalCompanyOrg org) {
        if (org.getParent_id() == 0) {
            return org;
        } else {
            for (PortalCompanyOrg companyOrg : portalCompanyOrgs) {
                if (companyOrg.getPortal_company_org_id().equals(org.getParent_id())) {
                    return queryParentOrg(portalCompanyOrgs, companyOrg);
                }
            }
            return null;
        }
    }

    // null则设置默认值
    public void setDefaultCount(QualityReport report) {
        report.setRequireCount(report.getRequireCount() == null ? 0 : report.getRequireCount());
        report.setExpressionCount(report.getExpressionCount() == null ? 0 : report.getExpressionCount());
        report.setEditCount(report.getEditCount() == null ? 0 : report.getEditCount());
        report.setWithdrawCount(report.getWithdrawCount() == null ? 0 : report.getWithdrawCount());
    }

    // 按年月设置时间
    public void setReportTime(List<QualityReport> reportList, String requireType, Integer reportType) {
        //设置时间
        String createDate = DateUtils.dateParseString(new Date());
        String yearMonth = DateUtils.dateParseShortString(new Date()).substring(0, 7);
        for (QualityReport res : reportList) {
            res.setRequireType(requireType);
            res.setCreateDate(createDate);
            res.setCreateYearMonth(yearMonth);
            res.setReportType(reportType);
            //count为null时设置为0
            setDefaultCount(res);
        }
    }

    @Override
    public List<QualityReport> queryReport(QualityReport bean) {

        // 质量要求 专业组
        if (bean.getCreateWeekOfYear() == null) {
//            bean.setRequireType(ConstantsUtil.QAKRequireType.PROCESS);
            bean.setReportType(ConstantsUtil.QAKReportType.REPORT_MAJORGROUP);
        } else {
            // 质量要求周报过程类查询
            bean.setRequireType(ConstantsUtil.QAKRequireType.PROCESS);
            bean.setReportType(ConstantsUtil.QAKReportType.BASE_EEEK);
        }
        List<QualityReport> list = qualityIssueReportDao.queryBaseWeekReport(bean);

        //补充查询所缺少的专业组
        validAllMajorGroup(list);

        QualityReport sum = new QualityReport();
        sum.setProGroup("合计");

        Integer withdrawSum = 0;
        Integer editSum = 0;
        Integer expressionSum = 0;
        Integer requireSum = 0;
        if (null != list) {
            for (QualityReport report : list) {
                withdrawSum += report.getWithdrawCount();
                editSum += report.getEditCount();
                expressionSum += report.getExpressionCount();
                requireSum += report.getRequireCount();
            }
            sum.setWithdrawCount(withdrawSum);
            sum.setEditCount(editSum);
            sum.setExpressionCount(expressionSum);
            sum.setRequireCount(requireSum);

            list.add(sum);
        }
        return list;
    }

    //质量要求过程类 时间
    @Override
    public List<QualityReport> queryProcessReportTime(QualityReport bean) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(DateUtils.DATE_MONTH_FORMAT.parse(bean.getCreateYearMonth()));
        } catch (ParseException e) {
            logger.error("", e);
            throw new ServiceException("日期转换异常！");
        }
        calendar.add(Calendar.MONTH, -11);
        String startTime = DateUtils.dateParseShorYearMonthtString(calendar.getTime());
        bean.setStartTime(startTime);

//        bean.setRequireType(ConstantsUtil.QAKRequireType.PROCESS);
        bean.setReportType(ConstantsUtil.QAKReportType.REPORT_TIME);

        List<QualityReport> list = qualityIssueReportDao.queryProcessReportTime(bean);
        return list;
    }

    //质量要求过程类 部门
    @Override
    public List<QualityReport> queryProcessReportDepartment(QualityReport bean) {
//        bean.setRequireType(ConstantsUtil.QAKRequireType.PROCESS);
        bean.setReportType(ConstantsUtil.QAKReportType.REPORT_DEPARTMENT);
        List<QualityReport> list = qualityIssueReportDao.queryBaseWeekReport(bean);

        // 补充一级部门

        return addLostDep(list, bean.getDeps());
    }

    // 补充查询所缺少的专业组
    private void validAllMajorGroup(List<QualityReport> list) {
        List<QualityReport> listNew = new ArrayList<>();
        //查询所有专业组
        List<String> allMajorGroup = baseDataService.queryAllMajorGroup();
        if (null != list) {
            List<String> mgList = new ArrayList<>();
            for (QualityReport qr : list) {
                mgList.add(qr.getProGroup());
            }

            // 补充 专业组
            for (String str : allMajorGroup) {
                if (!mgList.contains(str)) {
                    QualityReport qr = new QualityReport();
                    qr.setProGroup(str);
                    qr.setRequireCount(0);
                    qr.setExpressionCount(0);
                    qr.setEditCount(0);
                    qr.setWithdrawCount(0);

                    list.add(qr);
                }
            }

            // 排序 显示
            for (String str : allMajorGroup) {
                for (QualityReport qr : list) {
                    if (str.equals(qr.getProGroup())) {
                        listNew.add(qr);
                    }
                }
            }
            list = listNew;
        }
    }

    //补充缺少部门
    private List<QualityReport> addLostDep(List<QualityReport> list, List<String> inDeps) {
        List<QualityReport> listNew = new ArrayList<>();
        // 查询所有一级部门
        List<String> allDep = qualityIssueReportDao.queryAllDepByCompanyCode(SpringWebUtils.getRequestCompany(), inDeps);
        if (null != list) {
            List<String> mgList = new ArrayList<>();
            for (QualityReport qr : list) {
                mgList.add(qr.getDepartment());
            }

            // 补充完整所有部门
            for (String str : allDep) {
                if (!mgList.contains(str)) {
                    QualityReport qr = new QualityReport();
                    qr.setDepartment(str);
                    qr.setRequireCount(0);
                    qr.setExpressionCount(0);
                    qr.setEditCount(0);
                    qr.setWithdrawCount(0);

                    list.add(qr);
                }
            }
            //排序 显示
            for (String str : allDep) {
                for (QualityReport qr : list) {
                    if (str.equals(qr.getDepartment())) {
                        listNew.add(qr);
                    }
                }
            }
        }
        return listNew;
    }
}
