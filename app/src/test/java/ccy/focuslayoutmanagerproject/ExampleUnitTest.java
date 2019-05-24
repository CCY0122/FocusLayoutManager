package ccy.focuslayoutmanagerproject;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDuration(){
        assertEquals(getDuration(10f),100+20);
        assertEquals(getDuration(50f),100+100);
        assertEquals(getDuration(100f),300);
        assertEquals(getDuration(150f),300 + 150);
        assertEquals(getDuration(200f),300 + 300);
    }

    public long getDuration(float distance){
        float onceCompleteScrollLength = 100;

        long minDuration = 100;
        long maxDuration = 300;
        long duration;
        float distanceFraction = (Math.abs(distance) / (onceCompleteScrollLength));
        if(distance <= onceCompleteScrollLength){
            duration = (long) (minDuration + (maxDuration - minDuration) * distanceFraction);
        }else {
            duration = (long) (maxDuration * distanceFraction);
        }

        return duration;
    }
}