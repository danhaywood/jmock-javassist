package com.danhaywood.jmock.javassist;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.lib.action.VoidAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JavassistImposteriserTest {

    private Imposteriser imposteriser = JavassistImposteriser.INSTANCE;

    private Invokable invokable;
    private Invocation invocation;


    @Before
    public void setUp() throws Exception {
        invokable = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                JavassistImposteriserTest.this.invocation = invocation;
                return "result";
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        invokable = null;
        invocation = null;
    }


    // //////////////////////////////////////


    @Test
    public void happyCaseWhenJdkInterface() {
        assertTrue(imposteriser.canImposterise(Runnable.class));
        final Runnable imposter = imposteriser.imposterise(invokable, Runnable.class);
        assertNotNull(imposter);
        imposter.run();
    }

    @Test
    public void happyCaseWhenJdkClass() {
        assertTrue(imposteriser.canImposterise(Date.class));
        final Date imposter = imposteriser.imposterise(invokable, Date.class);
        assertNotNull(imposter);
        imposter.toString();
    }


    // //////////////////////////////////////

    @Test
    public void cannotImposterisePrimitiveType() {
        assertFalse(imposteriser.canImposterise(int.class));
    }

    @Test
    public void cannotImposteriseVoidType() {
        assertFalse(imposteriser.canImposterise(void.class));
    }


    // //////////////////////////////////////


    public static abstract class AnAbstractNestedClass {
        @SuppressWarnings("UnusedDeclaration")
        public abstract String foo();
    }

    @Test
    public void happyCaseWhenAbstractClass() {
        assertTrue(imposteriser.canImposterise(AnAbstractNestedClass.class));
        final AnAbstractNestedClass imposter = imposteriser.imposterise(invokable, AnAbstractNestedClass.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }


    // //////////////////////////////////////



    public static class AnInnerClass {
        @SuppressWarnings("UnusedDeclaration")
        public String foo() {return "original result";}
    }

    @Test
    public void happyCaseWhenNonFinalInstantiableClass() {
        assertTrue(imposteriser.canImposterise(AnInnerClass.class));
        final AnInnerClass imposter = imposteriser.imposterise(invokable, AnInnerClass.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////


    public static final class AFinalClass {
        @SuppressWarnings("UnusedDeclaration")
        public String foo() {return "original result";}
    }

    @Test
    public void cannotImposteriseWhenFinalInstantiableClasses() {
        assertFalse(imposteriser.canImposterise(AFinalClass.class));
    }


    // //////////////////////////////////////



    public static class AClassWithAPrivateConstructor {
        @SuppressWarnings("unused")
        private AClassWithAPrivateConstructor(String someArgument) {}

        public String foo() {return "original result";}
    }

    @Test
    public void happyCaseWhenClassWithNonPublicConstructor() {
        assertTrue(imposteriser.canImposterise(AClassWithAPrivateConstructor.class));
        AClassWithAPrivateConstructor imposter =
                imposteriser.imposterise(invokable, AClassWithAPrivateConstructor.class);

        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }


    // //////////////////////////////////////



    @SuppressWarnings("ClassInitializerMayBeStatic")
    public static class ConcreteClassWithConstructorAndInstanceInitializer {
        {
            shouldNotBeCalled("instance initializer");
        }

        public ConcreteClassWithConstructorAndInstanceInitializer() {
            shouldNotBeCalled("constructor");
        }

        public String foo() {
            shouldNotBeCalled("method foo()");
            return null; // never reached
        }

        private static void shouldNotBeCalled(String exceptionMessageIfCalled) {
            throw new IllegalStateException(exceptionMessageIfCalled + " should not be called");
        }
    }

    @Test
    public void happyCaseWhenConcreteClassWithConstructorAndInitialisersThatShouldNotBeCalled() {
        assertTrue(imposteriser.canImposterise(ConcreteClassWithConstructorAndInstanceInitializer.class));
        ConcreteClassWithConstructorAndInstanceInitializer imposter =
                imposteriser.imposterise(invokable, ConcreteClassWithConstructorAndInstanceInitializer.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////



    public interface AnInterface {
        String foo();
    }

    @Test
    public void happyCaseWhenCustomInterface() {
        assertTrue(imposteriser.canImposterise(AnInterface.class));
        AnInterface imposter = imposteriser.imposterise(invokable, AnInterface.class);

        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////




    @Test
    public void happyCaseWhenClassInASignedJarFile() throws Exception {
        File jarFile = new File("src/test/resources/signed.jar");

        assertTrue(jarFile.exists());

        URL jarURL = jarFile.toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[]{jarURL});
        Class<?> typeInSignedJar = loader.loadClass("TypeInSignedJar");

        assertTrue(imposteriser.canImposterise(typeInSignedJar));
        Object o = imposteriser.imposterise(new VoidAction(), typeInSignedJar);

        assertTrue(typeInSignedJar.isInstance(o));
    }



    // //////////////////////////////////////


    public static class ClassWithFinalToStringMethod {
        @Override
        public final String toString() {
            return "you can't override me!";
        }
    }

    // See issue JMOCK-150
    @Test
    public void cannotImposteriseAClassWithAFinalToStringMethod() {
        assertFalse(imposteriser.canImposterise(ClassWithFinalToStringMethod.class));

        try {
            imposteriser.imposterise(new VoidAction(), ClassWithFinalToStringMethod.class);
            fail("should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {

        }
    }





    // //////////////////////////////////////


    public interface EmptyInterface {}

    public interface AnInterface2 {
        String foo();
    }


    // See issue JMOCK-145
    @Test
    public void worksAroundBugInCglibWhenAskedToImposteriseObject() {
        imposteriser.imposterise(new VoidAction(), Object.class);
        imposteriser.imposterise(new VoidAction(), Object.class, EmptyInterface.class);
        imposteriser.imposterise(new VoidAction(), Object.class, AnInterface2.class);
    }

    private static Object invokeMethod(Object object, Method method, Object... args) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        return method.invoke(object, args);
    }



    // //////////////////////////////////////



    // See issue JMOCK-256 (Github #36)
    @Test
    public void doesntDelegateFinalizeMethod() throws Exception {
        Invokable failIfInvokedAction = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                fail("invocation should not have happened");
                return null;
            }
        };

        Object imposter = imposteriser.imposterise(failIfInvokedAction, Object.class);
        invokeMethod(imposter, Object.class.getDeclaredMethod("finalize"));
    }

}