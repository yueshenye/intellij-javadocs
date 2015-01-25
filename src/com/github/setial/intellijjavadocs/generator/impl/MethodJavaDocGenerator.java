package com.github.setial.intellijjavadocs.generator.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.setial.intellijjavadocs.model.JavaDoc;
import com.github.setial.intellijjavadocs.model.settings.JavaDocSettings;
import com.github.setial.intellijjavadocs.model.settings.Level;
import com.github.setial.intellijjavadocs.utils.JavaDocUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.TypeConversionUtil;

/**
 * The type Method java doc generator.
 *
 * @author Sergey Timofiychuk
 */
public class MethodJavaDocGenerator extends AbstractJavaDocGenerator<PsiMethod> {

    /**
     * Instantiates a new Method java doc generator.
     *
     * @param project the Project
     */
    public MethodJavaDocGenerator(@NotNull Project project) {
        super(project);
    }

    @Nullable
    @Override
    protected JavaDoc generateJavaDoc(@NotNull PsiMethod element) {
        if (!shouldGenerate(element) || !shouldGenerate(element.getModifierList())) {
            return null;
        }

        PsiMethod[] superMethods = element.findSuperMethods();
        if (superMethods.length > 0) {
            PsiMethod superMethod = superMethods[0];
            StringBuilder docBuilder = new StringBuilder();
            docBuilder.append("/**\n");
            docBuilder.append("* @see ");
            if (superMethod.getContainingClass().getQualifiedName().startsWith("java.lang")) {
                docBuilder.append(superMethod.getContainingClass().getName());
            } else {
                docBuilder.append(superMethod.getContainingClass().getQualifiedName());
            }
            docBuilder.append("#");
            docBuilder.append(superMethod.getName());
            docBuilder.append("(");
            PsiParameter[] parameters = superMethod.getParameterList().getParameters();

            docBuilder.append(Joiner.on(", ").join(
                Collections2.transform(Arrays.asList(parameters),
                    new Function<PsiParameter, String>() {
                        @Override
                        public String apply(@Nullable PsiParameter psiParameter) {
                            PsiType type = TypeConversionUtil.erasure(psiParameter.getType());
                            if (type.getCanonicalText().startsWith("java.lang")) {
                                return type.getPresentableText();
                            } else {
                                return type.getCanonicalText();
                            }
                        }
                    })));

            docBuilder.append(")\n");
            docBuilder.append("*/");

            return JavaDocUtils.toJavaDoc(docBuilder.toString(), getPsiElementFactory());
        } else {
            Template template = getDocTemplateManager().getMethodTemplate(element);
            Map<String, String> paramNames = new HashMap<String, String>();
            for (PsiParameter parameter : element.getParameterList().getParameters()) {
                paramNames.put(parameter.getName(),
                    getDocTemplateProcessor().buildDescription(parameter.getName(), false));
            }
            Map<String, String> exceptionNames = new HashMap<String, String>();
            for (PsiJavaCodeReferenceElement exception : element.getThrowsList()
                .getReferenceElements()) {
                exceptionNames.put(exception.getReferenceName(), getDocTemplateProcessor()
                    .buildDescription(exception.getReferenceName(), false));
            }
            String returnDescription = StringUtils.EMPTY;
            PsiTypeElement returnElement = element.getReturnTypeElement();
            if (returnElement != null) {
                returnDescription = returnElement.getText();
            }
            Map<String, Object> params = getDefaultParameters(element);
            if (returnElement != null) {
                params.put("isNotVoid", !returnElement.getType().isAssignableFrom(PsiType.VOID));
                params.put("return",
                    getDocTemplateProcessor().buildDescription(returnDescription, false));
            }
            params.put("paramNames", paramNames);
            params.put("exceptionNames", exceptionNames);

            String javaDocText = getDocTemplateProcessor().merge(template, params);
            return JavaDocUtils.toJavaDoc(javaDocText, getPsiElementFactory());
        }
    }

    private boolean shouldGenerate(@NotNull PsiMethod element) {
        PsiMethod[] superMethods = element.findSuperMethods();
        JavaDocSettings configuration = getSettings().getConfiguration();
        boolean overriddenMethods = superMethods.length > 0 && configuration != null
                                    && !configuration.getGeneralSettings().isOverriddenMethods();
        boolean level = configuration != null
                        && configuration.getGeneralSettings().getLevels().contains(Level.METHOD);
        return !level || !overriddenMethods;
    }

}
