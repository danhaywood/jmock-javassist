package com.danhaywood.jmock.javassist;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;


public class DemoTest {

    public static class Collaborating {
        final Collaborator collaborator;
        public Collaborating(final Collaborator collaborator) {
            this.collaborator = collaborator;
        }
        public String collaborateWithCollaborator() {
            return collaborator.foo();
        }
        public final String finalFoo() {
            return collaborator.finalFoo();
        }
    }

    public static class Collaborator {
        public String foo() {
            return "foo";
        }
        public final String finalFoo() {
            return "finalFoo";
        }
    }

    @Rule
    public JUnitRuleMockery simpleContext = new JUnitRuleMockery();

    @Rule
    public JUnitRuleMockery classMockingContext = new JUnitRuleMockery() {{
        setImposteriser(JavassistImposteriser.INSTANCE);
    }};


    @Test
    public void happyCase() {
        final Collaborator mockCollaborator = classMockingContext.mock(Collaborator.class);
        final Collaborating collaborating = new Collaborating(mockCollaborator);

        classMockingContext.checking(new Expectations() {{
            oneOf(mockCollaborator).foo();
            will(returnValue("somethingElse"));
        }});
        Assert.assertEquals("somethingElse", collaborating.collaborateWithCollaborator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotMockWithoutImposteriser() {
        simpleContext.mock(Collaborator.class);
    }


    @Test
    public void cannotMockFinalMethod() {
        final Collaborator mockCollaborator = classMockingContext.mock(Collaborator.class);
        final Collaborating collaborating = new Collaborating(mockCollaborator);

        classMockingContext.checking(new Expectations() {{
            oneOf(mockCollaborator).finalFoo();
            will(returnValue("somethingElse"));
        }});
        Assert.assertEquals("finalFoo", collaborating.finalFoo());
    }

}