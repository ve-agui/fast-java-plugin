package cn.anseon.dialog;

import cn.anseon.constants.CommonConstants;
import cn.anseon.domain.FastDomain;
import cn.anseon.domain.Property;
import cn.anseon.proxy.CodeGenerateProxy;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Map;

/**
 * Fast-Java Dialog
 *
 * @author GR
 * @date 2021-1-5 17:28
 */
public class FastJavaPopDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField fastClassName;
    private JTextArea fastClassProperty;
    private JCheckBox tipsTickDependsOnCheckBox;
    /**
     * 右键绝对路劲
     */
    private final String actionAbsoluteDir;
    /**
     * 右键main/java 下的路劲
     */
    private final String actionDir;

    public FastJavaPopDialog(Project project, String actionAbsoluteDir, String actionDir) {
        super(project);
        super.init();
        super.setTitle("New Fast Java");
        this.actionAbsoluteDir = actionAbsoluteDir;
        this.actionDir = actionDir;
        // 自动聚焦
        fastClassName.grabFocus();
        fastClassName.requestFocusInWindow();

    }

    @Override
    protected void doOKAction() {
        // 获取输入的类名
        String fastClassNameText = fastClassName.getText();
        if (StringUtils.isBlank(fastClassNameText)) {
            return;
        }

        if (fastClassNameText.endsWith(".java")) {
            fastClassNameText = fastClassNameText.replace(".java", "");
        }

        // 校验类名以DTO结尾
        if (fastClassNameText.length() > 3) {
            String afterThreeSuffix = fastClassNameText.substring(fastClassNameText.length() - 3);
            if (CommonConstants.DOMAIN_DTO_SUFFIX.equals(afterThreeSuffix.toUpperCase())) {
                fastClassNameText = fastClassNameText.substring(0, fastClassNameText.length() - 3);
            }
        }
        // 校验类名以DO、VO结尾
        else if (fastClassNameText.length() > 2) {
            String afterTwoSuffix = fastClassNameText.substring(fastClassNameText.length() - 2);
            if (CommonConstants.DOMAIN_DO_SUFFIX.equals(afterTwoSuffix.toUpperCase()) || CommonConstants.DOMAIN_VO_SUFFIX.equals(afterTwoSuffix.toUpperCase())) {
                fastClassNameText = fastClassNameText.substring(0, fastClassNameText.length() - 2);
            }
        }

        // 属性json
        String fastClassPropertyText = fastClassProperty.getText();
        // 构建fast java参数
        FastDomain fastDomain = new FastDomain()
                .setActionDir(actionDir)
                .setActionAbsoluteDir(actionAbsoluteDir)
                .setDependFastJava(false)
                .setFastJavaClassName(fastClassNameText)
                .setPropertyList(new ArrayList<>());

        // 是否依赖fast-java库
        if (tipsTickDependsOnCheckBox.isSelected()) {
            fastDomain.setDependFastJava(true);
        }

        // create property
        this.buildProperty(fastDomain, fastClassPropertyText);

        // 执行代码生成
        CodeGenerateProxy.getInstance().run(fastDomain);
        super.close(OK_EXIT_CODE);
    }

    /**
     * 构建属性
     *
     * @param fastDomain            对象
     * @param fastClassPropertyText 属性json字符串
     */
    private void buildProperty(FastDomain fastDomain, String fastClassPropertyText) {
        if (StringUtils.isBlank(fastClassPropertyText)) {
            return;
        }

        ArrayList<Property> propertyList = new ArrayList<>();
        Map map = JSON.parseObject(fastClassPropertyText, Map.class);
        for (Object key : map.keySet()) {
            Property property = new Property();
            property.setName(key.toString());
            Object value = map.get(key);
            String typeName = value.getClass().getTypeName();
            // 获取属性类型
            typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
            if ("BigDecimal".equals(typeName)) {
                typeName = "Double";
            }

            if ("JSONObject".equals(typeName)) {
                typeName = "Object";
            }

            if ("JSONArray".equals(typeName)) {
                typeName = "List";
            }

            property.setType(typeName);
            propertyList.add(property);
        }
        fastDomain.setPropertyList(propertyList);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
