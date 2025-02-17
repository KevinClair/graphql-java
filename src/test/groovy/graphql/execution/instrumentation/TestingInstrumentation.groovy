package graphql.execution.instrumentation

import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.execution.ExecutionContext
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldCompleteParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters
import graphql.language.Document
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLSchema
import graphql.validation.ValidationError

import java.util.concurrent.CompletableFuture

class TestingInstrumentation implements Instrumentation {

    def instrumentationState = new InstrumentationState() {}
    List<String> executionList = []
    List<Throwable> throwableList = []
    List<DataFetchingEnvironment> dfInvocations = []
    List<Class> dfClasses = []
    def capturedData = [:]

    def useOnDispatch = false

    @Override
    InstrumentationState createState() {
        return instrumentationState
    }

    @Override
    InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        new TestingInstrumentContext("execution", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("parse", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("validation", executionList, throwableList, useOnDispatch)
    }

    @Override
    ExecutionStrategyInstrumentationContext beginExecutionStrategy(InstrumentationExecutionStrategyParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingExecutionStrategyInstrumentationContext("execution-strategy", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("execute-operation", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<ExecutionResult> beginSubscribedFieldEvent(InstrumentationFieldParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("subscribed-field-event-$parameters.field.name", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<ExecutionResult> beginField(InstrumentationFieldParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("field-$parameters.field.name", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<Object> beginFieldFetch(InstrumentationFieldFetchParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("fetch-$parameters.field.name", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<CompletableFuture<ExecutionResult>> beginFieldComplete(InstrumentationFieldCompleteParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("complete-$parameters.field.name", executionList, throwableList, useOnDispatch)
    }

    @Override
    InstrumentationContext<CompletableFuture<ExecutionResult>> beginFieldListComplete(InstrumentationFieldCompleteParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return new TestingInstrumentContext("complete-list-$parameters.field.name", executionList, throwableList, useOnDispatch)
    }

    @Override
    GraphQLSchema instrumentSchema(GraphQLSchema schema, InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return schema
    }

    @Override
    ExecutionInput instrumentExecutionInput(ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return executionInput
    }

    @Override
    ExecutionContext instrumentExecutionContext(ExecutionContext executionContext, InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return executionContext
    }

    @Override
    DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        dfClasses.add(dataFetcher.getClass())
        return new DataFetcher<Object>() {
            @Override
            Object get(DataFetchingEnvironment environment) {
                dfInvocations.add(environment)
                dataFetcher.get(environment)
            }
        }
    }

    @Override
    CompletableFuture<ExecutionResult> instrumentExecutionResult(ExecutionResult executionResult, InstrumentationExecutionParameters parameters) {
        assert parameters.getInstrumentationState() == instrumentationState
        return CompletableFuture.completedFuture(executionResult)
    }
}

