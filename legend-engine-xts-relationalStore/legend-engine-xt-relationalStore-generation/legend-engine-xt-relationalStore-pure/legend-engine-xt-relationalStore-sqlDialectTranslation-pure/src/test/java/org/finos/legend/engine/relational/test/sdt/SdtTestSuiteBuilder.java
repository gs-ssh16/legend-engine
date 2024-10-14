// Copyright 2024 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.relational.test.sdt;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.pure.generated.*;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.ConcreteFunctionDefinition;
import org.finos.legend.pure.m3.coreinstance.meta.relational.metamodel.SQLNull;
import org.finos.legend.pure.m4.exception.PureException;
import org.finos.legend.pure.runtime.java.compiled.execution.CompiledExecutionSupport;
import org.finos.legend.pure.runtime.java.extension.store.relational.compiled.RelationalNativeImplementation;
import org.finos.legend.pure.runtime.java.extension.store.relational.compiled.natives.ResultSetValueHandlers;
import org.junit.Assert;

import java.sql.*;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.function.Function;

import static org.finos.legend.engine.test.shared.framework.PureTestHelperFramework.getClassLoaderExecutionSupport;
import static org.finos.legend.pure.generated.core_external_store_relational_sql_dialect_translation_sqlDialectTest_sdtFramework.*;
import static org.finos.legend.pure.generated.platform_pure_essential_meta_graph_elementToPath.*;

public class SdtTestSuiteBuilder
{
    private static final Set<String> STANDARD_SDT_TEST_PACKAGES = Sets.fixedSize.of("meta::external::store::relational::sqlDialectTranslation::sdtSuite");

    public static TestSuite buildSdtTestSuite(String dbType, Function<CompiledExecutionSupport, RichIterable<? extends Root_meta_pure_extension_Extension>> extensionsFunc, Set<String> extraSdtTestPackages, Set<String> excludedTests, Function0<Connection> connectionAcquisitionFunc)
    {
        final CompiledExecutionSupport es = getClassLoaderExecutionSupport();
        RichIterable<? extends Root_meta_pure_extension_Extension> extensions = extensionsFunc.apply(es);
        TestSuite suite = new TestSuite();
        Set<String> allSdtTestPackages = Sets.mutable.empty();
        allSdtTestPackages.addAll(STANDARD_SDT_TEST_PACKAGES);
        allSdtTestPackages.addAll(extraSdtTestPackages);
        allSdtTestPackages.forEach(pkg ->
        {
            RichIterable<? extends ConcreteFunctionDefinition<?>> sdtTestInPackage = Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_collectSDTTestsInPackage_String_1__ConcreteFunctionDefinition_MANY_(pkg, es);
            sdtTestInPackage.select(
                    x -> !excludedTests.contains(Root_meta_pure_functions_meta_elementToPath_PackageableElement_1__String_1_(x, es))
            ).forEach(x -> suite.addTest(new SDTTestCase(x, dbType, extensions, es, connectionAcquisitionFunc)));
        });
        return suite;
    }

    public static final class SDTTestCase extends TestCase
    {
        ConcreteFunctionDefinition<?> func;
        String dbType;
        RichIterable<? extends Root_meta_pure_extension_Extension> extensions;
        CompiledExecutionSupport es;
        Function0<Connection> connectionAcquisitionFunc;

        public SDTTestCase()
        {
        }

        public SDTTestCase(ConcreteFunctionDefinition<?> func, String dbType, RichIterable<? extends Root_meta_pure_extension_Extension> extensions, CompiledExecutionSupport es,  Function0<Connection> connectionAcquisitionFunc)
        {
            super(Root_meta_pure_functions_meta_elementToPath_PackageableElement_1__String_1_(func, es));
            this.dbType = dbType;
            this.extensions = extensions;
            this.func = func;
            this.es = es;
            this.connectionAcquisitionFunc = connectionAcquisitionFunc;
        }

        @Override
        protected void runTest() throws Throwable
        {
            Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_SqlDialectTest sdt = Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_getSqlDialectTest_ConcreteFunctionDefinition_1__SqlDialectTest_1_(this.func, this.es);
            MutableList<? extends String> setupSqlStatements = Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_generateSetupSqlsForSdtTestCase_SqlDialectTest_1__String_1__Extension_MANY__String_MANY_(
                    sdt,
                    this.dbType,
                    this.extensions,
                    this.es
            ).toList();
            MutableList<? extends String> teardownSqlStatements = Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_generateTeardownSqlsForSdtTestCase_SqlDialectTest_1__String_1__Extension_MANY__String_MANY_(
                    sdt,
                    this.dbType,
                    this.extensions,
                    this.es
            ).toList();
            String testSql = Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_generateTestSqlForSdtTestCase_SqlDialectTest_1__String_1__Extension_MANY__String_1_(
                    sdt,
                    this.dbType,
                    this.extensions,
                    this.es
            );
            System.out.print("EXECUTING " + this.getName() + " ... ");
            long start = System.nanoTime();

            Connection connection = null;
            Statement statement = null;
            Boolean testPass = null;
            try
            {
                connection = connectionAcquisitionFunc.get();
                statement = connection.createStatement();

                for (String setupStmt : setupSqlStatements)
                {
                    statement.execute(setupStmt);
                }

                try (ResultSet resultSet = statement.executeQuery(testSql))
                {
                    Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_TestResult actualResult = buildTestResultFromResultSet(resultSet, this.es);
                    try
                    {
                        Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_assertSdtTestPasses_SqlDialectTest_1__TestResult_1__Boolean_1_(sdt, actualResult, this.es);
                        testPass = true;
                    }
                    catch (PureException pureException)
                    {
                        testPass = false;
                        Assert.fail(pureException.getMessage());
                    }
                }
            }
            finally
            {
                if (statement != null)
                {
                    for (String setupStmt : teardownSqlStatements)
                    {
                        try
                        {
                            statement.execute(setupStmt);
                        }
                        catch (Exception e)
                        {
                            // Run remaining teardown stmts without failing
                            System.out.println(e.getMessage());
                        }
                    }

                    statement.close();
                    connection.close();

                    System.out.format("%s (%.6fs)\n", (testPass == null ? "ERROR" : testPass ? "DONE" : "FAIL"), (System.nanoTime() - start) / 1_000_000_000.0);
                    System.out.println("Executed SQL: " + testSql);
                }
            }
        }

        private static Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_TestResult buildTestResultFromResultSet(ResultSet resultSet, CompiledExecutionSupport es) throws SQLException
        {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            Root_meta_relational_metamodel_execute_ResultSet pureResult = new Root_meta_relational_metamodel_execute_ResultSet_Impl("");
            SQLNull sqlNull = new Root_meta_relational_metamodel_SQLNull_Impl("SQLNull");

            int count = resultSetMetaData.getColumnCount();
            MutableList<String> columns = FastList.newList(count);
            for (int i = 1; i <= count; i++)
            {
                String column = resultSetMetaData.getColumnLabel(i);
                columns.add(column);
            }
            pureResult._columnNames(columns);

            ListIterable<ResultSetValueHandlers.ResultSetValueHandler> handlers = ResultSetValueHandlers.getHandlers(resultSetMetaData);
            MutableList<Root_meta_relational_metamodel_execute_Row> rows = FastList.newList();
            while (resultSet.next())
            {
                MutableList<Object> rowValues = RelationalNativeImplementation.processRow(resultSet, handlers, sqlNull, new GregorianCalendar());
                rows.add((new Root_meta_relational_metamodel_execute_Row_Impl(""))._valuesAddAll(rowValues)._parent(pureResult));
            }
            pureResult._rows(rows);

            return Root_meta_external_store_relational_sqlDialectTranslation_sdtFramework_testResultFromResultSet_ResultSet_1__TestResult_1_(pureResult, es);
        }
    }
}
