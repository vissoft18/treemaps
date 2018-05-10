import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import math
import scipy

# unavoidable movement
def unavoidable_envelope():
    w_array = np.array([])
    h_array = np.array([])
    a_array = np.array([])

    n_samples = 20
    for w in np.linspace(0.000001, 1, n_samples, endpoint=False):
        for h in np.linspace(0.000001, 1, n_samples, endpoint=False):
            for a in np.linspace(0.000001, 1, n_samples, endpoint=False):
                w_array = np.append(w_array, w)
                h_array = np.append(h_array, h)
                a_array = np.append(a_array, a)



    df = pd.DataFrame({'w': w_array, 'h': h_array, 'a': a_array}, columns=['w', 'h', 'a'])

    #df = pd.DataFrame(np.random.rand(100, 4), columns=['w1', 'h1', 'w2', 'h2'])
    unavoidable_movement_test(df)

    plt.scatter(df['delta_data'], df['unavoidable'])

    # df.apply(lambda r: plt.annotate(str(r['a']), (r['delta_data'], r['unavoidable'])), axis=1)
    plt.xlim(0, 1)
    plt.ylim(0, 1)

    plt.show()


def point_hyperbole_dist(x, w, h, a):
    # Distance between a point (w,h) and a hyperbole y = a/4x where a is the area we are trying to reach
    return math.sqrt((x - w / 2) ** 2 + (a / (4 * x) - h / 2) ** 2)


def unavoidable_travel(*args):
    w1, h1, a = args
    if abs(h1 * w1 - a) < 0.00001:
        return 0
    else:
        result = scipy.optimize.minimize(point_hyperbole_dist, x0=w1, args=(w1, h1, a), method='Nelder-Mead')
        optimum_x = result.x[0]
        # Minimum corner travel is 4 times the minimum point-hyperbole distance
        return point_hyperbole_dist(optimum_x, w1, h1, a) * 4


def unavoidable_movement_test(df):
    norm = 5.65685424949 # 4 * math.sqrt(2)
    df['unavoidable'] = df.apply(lambda r: unavoidable_travel(*list(r)) / norm, axis=1)
    df['delta_data'] = abs(df['w'] * df['h'] - df['a'])
    return df