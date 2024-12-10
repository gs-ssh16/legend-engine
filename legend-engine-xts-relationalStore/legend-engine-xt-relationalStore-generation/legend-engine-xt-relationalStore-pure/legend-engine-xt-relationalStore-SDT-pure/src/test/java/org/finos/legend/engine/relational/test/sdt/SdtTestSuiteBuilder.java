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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.plan.execution.stores.relational.connection.tests.api.TestConnectionIntegrationLoader;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.DatabaseType;
import org.finos.legend.engine.test.shared.framework.TestServerResource;
import org.finos.legend.pure.generated.*;
import org.finos.legend.pure.m3.coreinstance.meta.pure.functions.collection.List;
import org.finos.legend.pure.m3.coreinstance.meta.pure.functions.collection.Pair;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.ConcreteFunctionDefinition;
import org.finos.legend.pure.runtime.java.compiled.execution.CompiledExecutionSupport;
import org.junit.Assert;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.finos.legend.engine.test.shared.framework.PureTestHelperFramework.getClassLoaderExecutionSupport;
import static org.finos.legend.engine.test.shared.framework.PureTestHelperFramework.wrapSuite;
import static org.finos.legend.pure.generated.core_external_store_relational_sdt_sdtFramework.*;
import static org.finos.legend.pure.generated.platform_pure_essential_meta_graph_elementToPath.*;

public class SdtTestSuiteBuilder
{
    private static final Set<String> SDT_TEST_PACKAGES = Sets.fixedSize.of(
            "meta::external::store::relational::sdt::suite"
    );

    public static Test buildSdtTestSuite(String dbType, Function<CompiledExecutionSupport, RichIterable<? extends Root_meta_pure_extension_Extension>> extensionsFunc, Function<CompiledExecutionSupport, RichIterable<? extends Pair<? extends String, ? extends List<? extends String>>>> expectedErrorsFunc)
    {
        final CompiledExecutionSupport es = getClassLoaderExecutionSupport();
        RichIterable<? extends Root_meta_pure_extension_Extension> extensions = extensionsFunc.apply(es);
        RichIterable<? extends Pair<? extends String, ? extends List<? extends String>>> expectedErrors = expectedErrorsFunc.apply(es);
        TestSuite suite = new TestSuite();
        SDT_TEST_PACKAGES.forEach(pkg ->
        {
            RichIterable<? extends ConcreteFunctionDefinition<?>> sdtTestInPackage = Root_meta_external_store_relational_sdt_framework_collectSDTTestsInPackage_String_1__ConcreteFunctionDefinition_MANY_(pkg, es);
            sdtTestInPackage.forEach(x ->
            {
                String elementPath = platform_pure_essential_meta_graph_elementToPath.Root_meta_pure_functions_meta_elementToPath_PackageableElement_1__String_1_(x, es);
                suite.addTest(new SDTTestCase(x, dbType, extensions, es, expectedErrors.select(e -> e._first().equals(elementPath)).getAny()));
            });
        });
        return wrapSuite(
                () -> true,
                () -> suite,
                () -> false,
                Lists.mutable.with((TestServerResource) TestConnectionIntegrationLoader.extensions().select(c -> c.getDatabaseType() == DatabaseType.valueOf(dbType)).getFirst())
        );
    }

    public static final class SDTTestCase extends TestCase
    {
        ConcreteFunctionDefinition<?> func;
        String dbType;
        RichIterable<? extends Root_meta_pure_extension_Extension> extensions;
        CompiledExecutionSupport es;
        Pair<? extends String, ? extends List<? extends String>> expectedErrors;

        public SDTTestCase()
        {
        }

        public SDTTestCase(ConcreteFunctionDefinition<?> func, String dbType, RichIterable<? extends Root_meta_pure_extension_Extension> extensions, CompiledExecutionSupport es, Pair<? extends String, ? extends List<? extends String>> expectedErrors)
        {
            super(Root_meta_pure_functions_meta_elementToPath_PackageableElement_1__String_1_(func, es));
            this.dbType = dbType;
            this.extensions = extensions;
            this.func = func;
            this.es = es;
            this.expectedErrors = expectedErrors;
        }

        @Override
        protected void runTest() throws Throwable
        {
            MutableList<? extends Root_meta_external_store_relational_sdt_framework_SqlDialectTest> tests = Root_meta_external_store_relational_sdt_framework_getSqlDialectTests_ConcreteFunctionDefinition_1__SqlDialectTest_MANY_(this.func, this.es).toList();
            int testCount = tests.size();

            for (int i = 0; i < testCount; ++i)
            {
                System.out.print("EXECUTING " + this.getName() + "(" + (i + 1) + "/" + testCount + ") ... ");
                long start = System.nanoTime();

                boolean testPass = false;
                try
                {
                    Root_meta_external_store_relational_sdt_framework_runSqlDialectTest_SqlDialectTest_1__String_1__Extension_MANY__DebugContext_1__Boolean_1_(
                            tests.get(i),
                            this.dbType,
                            this.extensions,
                            new Root_meta_pure_tools_DebugContext_Impl("")._debug(false),
                            this.es
                    );
                    testPass = true;
                }
                catch (Exception e)
                {
                    if (expectedErrors != null)
                    {
                        String errorMessage = e.getMessage();
                        Pattern p = Pattern.compile("Assert failure at \\((.*?)\\), (.*)", Pattern.DOTALL);
                        Matcher m = p.matcher(errorMessage);
                        if (m.matches())
                        {
                            // Check assert message
                            String assertMessage = m.group(2);
                            Assert.assertTrue(expectedErrors._second()._values().contains(assertMessage.startsWith("\"") ? assertMessage.substring(1, assertMessage.length() - 1) : assertMessage));
                        }
                        else
                        {
                            Assert.assertTrue(expectedErrors._second()._values().contains(e.getMessage()));
                        }
                        testPass = true;
                        return;
                    }
                    throw e;
                }
                finally
                {
                    System.out.format("%s (%.6fs)\n", (testPass ? "DONE" : "FAIL"), (System.nanoTime() - start) / 1_000_000_000.0);
                }
            }
        }
    }
}
