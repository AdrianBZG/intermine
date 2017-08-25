package org.intermine.objectstore;

/*
 * Copyright (C) 2002-2016 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.DynamicUtil;
import org.intermine.util.XmlBinding;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility functions for creating ObjectStore test infrastructure.
 */
public class ObjectStoreTestUtils {
    protected static List toList(Object[][] o) {
        List rows = new ArrayList();
        for(int i=0;i<o.length;i++) {
            rows.add(new ResultsRow(Arrays.asList((Object[])o[i])));
        }
        return rows;
    }

    public static Collection loadItemsFromXml(Model model, String resourceName) throws Exception {
        XmlBinding binding = new XmlBinding(model);
        return binding.unmarshal(SetupDataTestCase.class.getClassLoader().getResourceAsStream(resourceName));
    }

    public static void setIdsOnItems(Collection c) throws Exception {
        int i=1;
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            try {
                DynamicUtil.setFieldValue(iter.next(), "id", new Integer(i++));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public static Map mapItemsToNames(Collection c) throws Exception {
        Map returnData = new LinkedHashMap();
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            Object o = iter.next();
            returnData.put(simpleObjectToName(o), o);
        }
        return returnData;
    }

    public static Object simpleObjectToName(Object o) throws Exception {
        Method name = null;
        try {
            name = o.getClass().getMethod("getName", new Class[] {});
        } catch (Exception e) {
            try {
                name = o.getClass().getMethod("getAddress", new Class[] {});
            } catch (Exception e2) {
            }
        }
        if (name != null) {
            return name.invoke(o, new Object[] {});
        } else if (o instanceof InterMineObject) {
            return new Integer(o.hashCode());
        } else {
            return o;
        }
    }

    public static void storeData(ObjectStoreWriter dataWriter, Map data) throws Exception {
        //checkIsEmpty();
        System.out.println("Storing data");
        long start = new Date().getTime();
        try {
            //Iterator iter = data.entrySet().iterator();
            //while (iter.hasNext()) {
            //    InterMineObject o = (InterMineObject) ((Map.Entry) iter.next())
            //        .getValue();
            //    o.setId(null);
            //}
            dataWriter.beginTransaction();
            Iterator iter = data.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object o = entry.getValue();
                dataWriter.store(o);
            }
            dataWriter.commitTransaction();
        } catch (Exception e) {
            dataWriter.abortTransaction();
            throw new Exception(e);
        }

        System.out.println("Took " + (new Date().getTime() - start) + " ms to set up data");
    }
}