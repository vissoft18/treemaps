/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.util.Random;

/**
 *
 * @author msondag
 */
public class Randomizer {

    private static Random random;
    private static boolean init = false;

    public static double getRandomDouble() {
        if (init == false) {
            init = true;
            random = new Random(0);
        }
        return random.nextDouble();
    }
}
