/*
 * Copyright 2013-2018 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoclet;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AsciidocletIntegrationTest
{
    /**
     * Running this test needs the following JVM argument:
     *      --add-exports jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED
     */
    @Test
    public void testJavadocIntegration() throws Exception
    {
        Method execute = Class.forName( "jdk.javadoc.internal.tool.Main" ).getMethod( "execute", String[].class );
        execute.setAccessible( true );
        String outputDirectory = "target/javadoc-output";
        deleteRecursively( outputDirectory );
        execute.invoke( null, (Object) new String[] {
                "--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=asciidoclet",
                "--add-exports=jdk.compiler/com.sun.tools.javac.parser=asciidoclet",
                "--add-exports=jdk.compiler/com.sun.tools.javac.tree=asciidoclet",
                "--add-exports=jdk.compiler/com.sun.tools.javac.model=asciidoclet",
                "--module-path", classpath(),
                "--limit-modules", "asciidoclet,java.base,jdk.javadoc,asciidoctorj,guava",
                "--class-path", classpath(),
                "-doclet", "org.asciidoclet.Asciidoclet",
                "--source-path", "src/main/java",
                "-d", outputDirectory,
                "--base-dir", ".",
                "org.asciidoclet",
        } );
    }

    private void deleteRecursively( String outputDirectory ) throws IOException
    {
        Path outputPath = Paths.get( outputDirectory );
        if ( Files.exists( outputPath ) )
        {
            Files.walkFileTree( outputPath, new SimpleFileVisitor<>()
            {
                @Override
                public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException
                {
                    Files.deleteIfExists( file );
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException
                {
                    Files.deleteIfExists( dir );
                    return FileVisitResult.CONTINUE;
                }
            } );
        }
    }

    private String classpath()
    {
        return Arrays.stream( System.getProperty( "java.class.path" ).split( ":" ) )
                .filter( s -> !s.contains( "ideaIU" ) ) // Filter out Intellij jar files.
                .collect( Collectors.joining( ":" ) );
    }
}