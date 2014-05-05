/*
 * Copyright 2013 David Kunzler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package de.devland.esperandro;

import de.devland.esperandro.tests.EsperandroDefaultsExample;
import de.devland.esperandro.tests.model.Container;
import junit.framework.Assert;
import org.fest.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DefaultsTest {

    @Test
    public void annotationDefaults() {
        EsperandroDefaultsExample preferences = Esperandro.getPreferences(EsperandroDefaultsExample.class,
                Robolectric.application);
        Assert.assertNotNull(preferences);

        Assert.assertTrue(preferences.boolPref());
        Assert.assertTrue(preferences.boolPref$Default(true));
        Assert.assertFalse(preferences.boolPref$Default(false));

        Assert.assertEquals(42, preferences.integerPref());

        Assert.assertEquals(42l, preferences.longPref());

        Assert.assertEquals(4.2f, preferences.floatPref());

        Assert.assertNull(preferences.stringSetPref());
        Set<String> exampleSet = new HashSet<String>();
        exampleSet.add("test");
        Set<String> preferenceSet = preferences.stringSetPref$Default(exampleSet);
        Assert.assertEquals(exampleSet, preferenceSet);
        Assert.assertEquals("test", preferenceSet.iterator().next());

        Assert.assertNull(preferences.complexPref());
        Container exampleComplex = new Container();
        exampleComplex.value = "test";
        exampleComplex.anotherValue = 23;
        Assert.assertEquals(exampleComplex, preferences.complexPref$Default(exampleComplex));
    }


}
