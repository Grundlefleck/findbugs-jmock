/*
 * FindBugs4JMock. Copyright (c) 2010 youDevise, Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package com.youdevise.fbplugins.jmock;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

public class JUnitTestClassVisitor extends EmptyVisitor {

    private final List<String> methodsWithJUnitTestAnnotation = new ArrayList<String>();
    private boolean foundTestMethods = false;
    private boolean isRunWithJMockTestRunner = false;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new JUnitTestMethodVisitor(name, desc);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if ("Lorg/junit/runner/RunWith;".equals(desc)) {
            return new RunWithAnnotationVisitor();
        }
        return super.visitAnnotation(desc, visible);
    }
    
    
    private class RunWithAnnotationVisitor extends EmptyVisitor {

        @Override
        public void visit(String name, Object value) {
            if(isRunWithJMockAnnotation(name, value)) {
                isRunWithJMockTestRunner = true;
            }
        }
        
        private boolean isRunWithJMockAnnotation(String name, Object value) {
            if(! (value instanceof Type)) return false;
            
            Type testRunnerType = (Type) value;
            String typeDescriptor = testRunnerType.getDescriptor();
            return "Lorg/jmock/integration/junit4/JMock;".equals(typeDescriptor);
        }
        
    }
    
    private class JUnitTestMethodVisitor extends EmptyVisitor {

        private final String name;
        private final String desc;

        public JUnitTestMethodVisitor(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if(isUnitTestMethod(desc)) {
                String methodDescriptor = this.name + ":" + this.desc;
                foundTestMethods = true;
                methodsWithJUnitTestAnnotation.add(methodDescriptor);
            }
            return this;
        }

        private boolean isUnitTestMethod(String annotationType) {
            return "Lorg/junit/Test;".equals(annotationType);
        }
    }
    
    public boolean isJUnitTestMethod(Method method) {
        String methodDescriptor = method.getName() + ":" + method.getSignature();
        return methodsWithJUnitTestAnnotation.contains(methodDescriptor);
    }

    public boolean hasFoundTestMethods() {
        return foundTestMethods;
    }

    public boolean isRunWithJMockTestRunner() {
        return isRunWithJMockTestRunner;
    }
}
