package com.botian.recognition;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        System.out.println(222);
        List setList = new ArrayList();
        List getlist = new ArrayList();
        getlist.add(1);
        getlist.add(2);
        getlist.add(3);
        getlist.add(4);
        getlist.add(5);
        getlist.add(6);
        getlist.add(7);
        getlist.add(8);
        getlist.add(9);
        getlist.add(10);
        getlist.add(11);
        getlist.add(12);
        setSinglePageList(setList, getlist, 13);
        System.out.println(setList);
    }

    /***分组*/
    private void setSinglePageList(List setList, List getList, int eachNum) {
        int tempRemainder = getList.size() % eachNum;
        int tempInteger   = getList.size() / eachNum;
        System.out.println(tempRemainder);
        System.out.println(tempInteger);
        for (int i = 0; i < (tempInteger); i++) {
            List indexList = new ArrayList();
            for (int m = i * eachNum; m < (i + 1) * eachNum; m++) {
                indexList.add(getList.get(m));
            }
            setList.add(indexList);
        }
        if ((tempRemainder) != 0) {
            //有余数
            List indexList = new ArrayList();
            for (int i = (tempInteger * eachNum); i < (tempInteger * eachNum + tempRemainder); i++) {
                indexList.add(getList.get(i));
            }
            setList.add(indexList);
        }
    }
}