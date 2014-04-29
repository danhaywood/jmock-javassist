jmock-javassist
===============

[![Build Status](https://travis-ci.org/danhaywood/jmock-javassist.png?branch=master)](https://travis-ci.org/danhaywood/jmock-javassist)

Implementation of JMock's Imposteriser interface using javassist.

Example usage:

    public class Collaborating {
        final Collaborator collaborator;
        public Collaborating(final Collaborator collaborator) {
            this.collaborator = collaborator;
        }
        public String collaborateWithCollaborator() {
            return collaborator.foo();
        }
    }

    public static class Collaborator {
        public String foo() {
            return "foo";
        }
    }

    public class DemoTest {

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
    }
