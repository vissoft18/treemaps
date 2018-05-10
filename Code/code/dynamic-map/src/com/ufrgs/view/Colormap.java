package com.ufrgs.view;

import java.awt.*;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class Colormap {


    private static Color[] sequential = {
            new Color(0.996f, 0.941f, 0.850f),
            new Color(0.992f, 0.8f, 0.541f),
            new Color(0.988f, 0.552f, 0.349f),
            new Color(0.890f, 0.290f, 0.2f),
            new Color(0.701f, 0f, 0f)
    };

    public static Color sequentialColormap(float v) {

        if (v == 0.0) {
            return new Color(0,0,0,0);
        }

        if (v > 1) {
            v = 1;
        }

        float d = v;
        while (d > 0.25) d -= 0.25;
        d *= 4;

        int f = (int) floor(v * 4);
        int c = (int) ceil(v * 4);

        return new Color (
                sequential[f].getRed()/255f * (1 - d) + sequential[c].getRed()/255f * (d),
                sequential[f].getGreen()/255f * (1 - d) + sequential[c].getGreen()/255f * (d),
                sequential[f].getBlue()/255f * (1 - d) + sequential[c].getBlue()/255f * (d)
        );
    }
}
