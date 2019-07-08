package com.faw_vk.qak.mgmt.util;

import com.efast.cafe.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConstantsUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConstantsUtil.class);

    /**
     * 默认公司代码
     */
    public final static String DEFAULT_COMPANY = "FAW_VK";
    public final static String Node_Type_MajorGroup = "group";//基础数据专业组
    public final static String Node_Type_System = "system";//基础数据系统
    public final static String Node_Type_Other = "other";//基础数据其他

    public final static String Error_Message_Car = "含有非法车型数据";//导入验证车型是有配置
    public final static String Error_Message_Part = "含有非法零件号数据";//导入验证零件是有配置

    public final static String Form_Status_Draft = "draft";//表据状态 草稿
    public final static String Form_Status_Submit = "submit";//表据状态 已提交

    public final static String Create_Type_Manual = "manual";//单据创建类型-手工创建
    public final static String Create_Type_Auto = "auto";//单据创建类型-接口创建

    public final static String Form_Type_Issue = "issue";//表单类型 -- 质量问题单

    public final static String QAK_DETAIL_EXCEL_LOGONAME = "FV_log.png";//导出excel中logo图片名称
    public final static String QAK_FAW_VK_DETAIL_EXCEL_TEMPLATE_CHINESE = "FAW_VK_Chinese_ExcelTemplate.xlsx";  //单据详情中文excel导出模版
    public final static String QAK_FAW_VK_DETAIL_EXCEL_TEMPLATE_GERMAN = "FAW_VK_GERMAN_ExcelTemplate.xlsx";  //单据详情德文excel导出模版
    public final static String QAK_FAW_VK_LIST_EXPORT_EXCEL_TEMPLATE = "ListExportExcelTemplate.xlsx";     //列表导出excel模版
    public final static String QAK_FAW_VK_DEFAULT_EXCEL_NAME = "质量问题单";     //列表导出excel默认名字

    public final static String T_QUALITY_ISSUE_ISSUE_TEMPLATE = "t_quality_issue_QT565-469";  //质量问题单模版code
    public final static String T_QUALITY_ISSUE_ISSUE_TEMPLATE_SELF = "t_quality_issue_QT565-509";  //质量问题单模版code
    public final static String T_QUALITY_ISSUE_EXPRE_TEMPLATE = "apiindex565-493";           //经验总结模版code
    public final static String T_QUALITY_ISSUE_EXPRE_TEMPLATE_SELF = "apiindex565-513";           //经验总结模版code
    public final static String T_QUALITY_ISSUE_QUALITY_TEMPLATE = "apiindex565-497";           //质量要求模版code

    //质量要求类型LOV
    public final static String T_QUALITY_ISSUE_TYPE_LOV = "QAK_QUALITY_TYPE";
    //问题来源LOV
    public final static String T_QUALITY_ISSUE_PROBLEM_RESOURCE_LOV = "QAK_PROBLEM_SOURCE";
    //问题分类类型LOV
    public final static String T_QUALITY_ISSUE_CLASSIFY_LOV = "QAK_CLASSIFY";
    //零件工艺LOV
    public final static String T_QUALITY_ISSUE_TECHNOLOGY_LOV = "QAK_TECHNOLOGY";
    //质量问题单状态LOV
    public final static String T_QUALITY_ISSUE_STATUS_LOV = "QAK_ISSUE_QUA_STATUS";
    //经验总结状态LOV
    public final static String T_QUALITY_ISSUE_EXPRE_STATUS_LOV = "QAK_EXPERIENCE_STATUS";
    //质量要求状态LOV
    public final static String T_QUALITY_ISSUE_QUALITY_STATUS_LOV = "QAK_REQUIRE_STATUS";
    //创建类型LOV
    public final static String T_QUALITY_ISSUE_CREATE_TYPE_LOV = "QAK_QUALITY_CREATE_TYPE";
    //满意度LOV
    public final static String T_QUALITY_ISSUE_QAK_DEGREE_LOV = "QAK_DEGREE";
    // 导致结果 德文 QAK_RESULT_GERMAN
    public final static String T_QUALITY_ISSUE_QAK_QAK_RESULT_GERMAN_LOV = "QAK_RESULT_GERMAN";
    /**
     * 专家/经理角色名
     */
    public static final String ROLE_MANAGER = "QAKManager";

    /**
     * 状态机审核原因key
     */
    public static final String STATEMACH_APPROVE_REASON_KEY = "remark";


    /**
     * 经验总结pdf groupID
     */
    public static final String ATTACHMENT_REQUIRED_PDF_GROUP_ID = "r_pdf";


    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        String string = format.format(new Date());
        string += UUID.randomUUID().toString().substring(0, 4);
        logger.info(string.substring(2, string.length()));
    }

    public static class QAKImgGroupId {
        public static String NEGATIVE = "negativeLogo";
        public static String POSITIVE = "positiveLogo";
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(NEGATIVE);
            list.add(POSITIVE);
        }
    }

    public static class QAKRequireType {
        public static String BASE = "basicRequire";    //基础质量要求
        public static String PROCESS = "processRequire"; //过程要求
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(BASE);
            list.add(PROCESS);
        }
    }

    //单据附件的groupId
    public static class QAKAttachmentGroupId {
        public static String EXPERIENCE_IMPORT_GROUPID = "experienceImport";
        public static String ISSUE_IMPORT_GROUPID = "issueImport";
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(EXPERIENCE_IMPORT_GROUPID);
            list.add(ISSUE_IMPORT_GROUPID);
        }
    }

    //单据附件的groupId
    public static class QAKFORMTYPE {
        public static String FORM_TYPE_ISSUE = "issue";
        public static String FORM_TYPE_EXPERIENCE = "experience";
        public static String FORM_TYPE_REQUIRE = "require";
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(FORM_TYPE_ISSUE);
            list.add(FORM_TYPE_EXPERIENCE);
        }
    }

    public static class QAKExcelLanguage {
        public static String CHANESE = "chinese";
        public static String GERMAN = "german";
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(CHANESE);
            list.add(GERMAN);
        }
    }

    public static class QAKExcelSheetName {
        public static String SHEET_CHANESE = "质量问题单";
        public static String SHEET_CHANESE_EXPERIENCE = "经验总结单";
        public static String SHEET_CHANESE_REQUIRE = "质量要求单";
        public static String SHEET_GERMAN = "Qualitätsproblemdokument";
        public static String SHEET_GERMAN_EXPERIENCE = "ZusammenfassungDerErfahrung";
        public static String SHEET_GERMAN_REQUIRE = "Qualitätsanforderungsdokument";
        public static List<String> list = new ArrayList<String>();

        static {
            list.add(SHEET_CHANESE);
            list.add(SHEET_GERMAN);
            list.add(SHEET_CHANESE_EXPERIENCE);
            list.add(SHEET_GERMAN_EXPERIENCE);
        }
    }

    public static class QAKReportType {
        public static Integer BASE_EEEK = 1;            //质量要求周报-过程类
        public static Integer REPORT_TIME = 2;        //质量要求-按时间
        public static Integer REPORT_DEPARTMENT = 3;    //质量要求-按部门
        public static Integer REPORT_MAJORGROUP = 4;    //质量要求-按专业组
        public static List<Integer> list = new ArrayList<Integer>();

        static {
            list.add(BASE_EEEK);
            list.add(REPORT_TIME);
            list.add(REPORT_DEPARTMENT);
            list.add(REPORT_MAJORGROUP);
        }
    }
}
