jmock-javassist
===============

[![Build Status](https://travis-ci.org/danhaywood/jmock-javassist.png?branch=master)](https://travis-ci.org/danhaywood/jmock-javassist)


Implementation of JMock's Imposteriser interface using javassist, to allow mocking of concrete classes.

This is an alternative to the cglib-based implementation that ships with JMock.

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


## Legal Stuff

### License

    Copyright 2014 Dan Haywood

    Licensed under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
