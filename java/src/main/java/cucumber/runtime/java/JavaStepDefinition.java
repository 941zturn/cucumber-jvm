package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.TransformLookup;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final Expression expression;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final List<ParameterInfo> parameterInfos;

    public JavaStepDefinition(Method method, String expression, long timeoutMillis, ObjectFactory objectFactory, TransformLookup transformLookup) {
        this.method = method;
        this.expression = new ExpressionFactory().createExpression(expression, getArgumentTypes(method), transformLookup);
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;

        this.parameterInfos = ParameterInfo.fromMethod(method);
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    public List<Argument> matchedArguments(Step step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step.getName());
    }

    private static List<Class<?>> getArgumentTypes(Method method) {
        List<Class<?>> types = new ArrayList<Class<?>>(method.getParameterTypes().length);
        for (Class<?> type : method.getParameterTypes()) {
            types.add(type);
        }
        return types;
    }

    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return expression.getSource();
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
