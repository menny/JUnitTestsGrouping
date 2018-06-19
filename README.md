# JUnitTestsGrouping
A JUnit Filter for sharding your JUnit tests.

`master` latest build: [![CircleCI](https://circleci.com/gh/menny/JUnitTestsGrouping/tree/master.svg?style=svg)](https://circleci.com/gh/menny/JUnitTestsGrouping/tree/master)<br/>
`master` coverage: [![codecov](https://codecov.io/gh/menny/JUnitTestsGrouping/branch/master/graph/badge.svg)](https://codecov.io/gh/menny/JUnitTestsGrouping)

## Usage
Using JUnit's `Suite` or `Filter` mechanism, JUnitTestsGrouping will skip tests that are not in the execution group/shard.
Basically, it calculated the group index base on the test-class name and if this index equals the execution index
the test will execute, otherwise it will be filtered out.

### Setup - Gradle
Add JUnitTestsGrouping dependency to your `build.gradle`:
```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    testImplementation 'com.github.menny:JUnitTestsGrouping:0.3.0'
}
```

### Setup - tests
There are two mechanisms in this library: `Filter` or `Suite`

### Setup - tests Filter
Add the filter to your JUnit test-runner. In this example, I created a custom `TestRunner`:
```
public class MyTestRunner extends RobolectricTestRunner {
    public MyTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, true);
    }
}
```

### Setup - tests Suite
Create an empty class in your tests folder (at the root of the package), and annotate it with `ShardingSuite` and the hashing strategy: 
```
@RunWith(ShardingSuite.class)
@ShardingSuite.ShardUsing(TestClassHashingStrategy.class)
public class MyTestsSuite {
}
```
Our `ShardingSuite` will scan your `ClassLoader` for all classes that have tests in them, and shard them using the `HashingStrategy` class
provided in `@ShardingSuite.ShardUsing`.
For Gradle, you will also need to tell Gradle that you want to run this Suite. From command line:
```
./gradlew -Dtest.single=MyTestsSuite clean test
```
Or, the preferred way, create a `Test` task:
```
task testWithSharding(type: Test) {
    include '**/MyTestsSuite.class'
}
```

<br/>

For both mechanisms (Filter or Suite) you will need to set the number of groups to split the tests between, and set the current test group index (also, in your `build.gradle`):
```
systemProperties['TestsGroupingFilter_TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY'] =  System.getenv().getOrDefault('TEST_GROUP_INDEX', 0);
systemProperties['TestsGroupingFilter_TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY'] = System.getenv().getOrDefault('TEST_GROUPS_COUNT', 1);
```

Where the environment variable`TEST_GROUP_INDEX` is the current test-group and `TEST_GROUPS_COUNT` is the total number of test groups.

## Example Scenario 1
So, an example could be:

 * a Continuous Integration setup where you have 3 machines for running your tests.
 * in each of those machines you will set up
   * an environment variable `TEST_GROUPS_COUNT=3`,
   * and `TEST_GROUP_INDEX` which holds the index of the machine (`TEST_GROUP_INDEX=0` for the first, `TEST_GROUP_INDEX=1`
for the second and `TEST_GROUP_INDEX=2` for the third).

## Extending for other grouping types
The grouping is done by the given `HashingStrategy`, which calculates a hash for the test's `Description` instance, and groups the tests according to that.

By default, `JUnitTestsGrouping` uses `TestClassHashingStrategy` to group tests: it hashes the test-class name. This ensures that tests that are defined in the same class will run in the same group.<br>
You can change that behavior by providing a different implementation of `HashingStrategy` when constructing `TestsGroupingFilter`.<br>
For example, you may want to group tests by an `Annotation`:
```
public class AnnotationHashingStrategy extends SimpleHashingStrategyBase {

    @Override
    public int calculateHashFromDescription(Description description) {
        final Class testClass = description.getTestClass();
        if (testClass.getAnnotation(Marker1.class) != null) return 0;
        if (testClass.getAnnotation(Marker2.class) != null) return 1;
        if (testClass.getAnnotation(Marker3.class) != null) return 2;
        
        return 3;
    }
}
```


Similar strategy is implemented in `AnnotationHashingStrategy`.

## Example Scenario 2 - this library
In this library, we use the `Suite` mechanism in a non-standard way: we use the `HashingStrategy` to filter out test-classes that are
used as input in the tests - when our [strategy](https://github.com/menny/JUnitTestsGrouping/blob/master/src/test/java/net/evendanan/testgrouping/inputs/TestableHashingStrategy.java)
sees classes in the `inputs` package, it returns `-1`, which will ensure these classes are not executed.


## License
```
    Copyright 2018 Menny Even-Danan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
