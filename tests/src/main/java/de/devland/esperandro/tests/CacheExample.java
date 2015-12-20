package de.devland.esperandro.tests;

import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;
import de.devland.esperandro.tests.model.ContainerListObject;

import java.util.ArrayList;

/**
 * Created by deekay on 16.12.2015.
 */
@SharedPreferences
@Cached
public interface CacheExample {
    String cachedValue();

    void cachedValue(String cachedValue);

    void containerList(ArrayList<Container> containerList);

    ArrayList<Container> containerList();

    ArrayList<Container> containerList$Default(ArrayList<Container> runtimeDefault);

    void containerListObject(ContainerListObject containerListObject);

    ContainerListObject containerListObject();
}