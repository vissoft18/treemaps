import pandas as pd
import math
import scipy


# Aspect ratio
def compute_aspect_ratios(t):
    return t[['w', 'h']].min(axis=1) / t[['w', 'h']].max(axis=1)


# delta_vis has the same definition as the normalized corner travel from the vis18 paper
def compute_delta_vis(t0, t1):
    base_width = (t0['x'] + t0['w']).max()
    base_height = (t0['y'] + t0['h']).max()
    # Normalize by 4 * hypotenuse
    norm = 4 * math.sqrt(base_width ** 2 + base_height ** 2)

    df = pd.merge(t0, t1, how='inner', left_index=True, right_index=True)
    df.columns = ['x0', 'y0', 'w0', 'h0', 'x1', 'y1', 'w1', 'h1']
    return df.apply(lambda r: corner_travel(*list(r)) / norm, axis=1)


def corner_travel(*args):
    x0, y0, w0, h0, x1, y1, w1, h1 = args
    return point_distance(x0, y0, x1, y1)   \
        + point_distance(x0 + w0, y0, x1 + w1, y1)   \
        + point_distance(x0, y0 + h0, x1, y1 + h1)   \
        + point_distance(x0 + w0, y0 + h0, x1 + w1, y1 + h1)


def point_distance(x0, y0, x1, y1):
    return math.sqrt((x0 - x1) ** 2 + (y0 - y1) ** 2)


# delta_data is the change in the normalized area of a cell
def compute_delta_data(t0, t1):
    base_width = (t0['x'] + t0['w']).max()
    base_height = (t0['y'] + t0['h']).max()
    norm = base_height * base_width  # Normalize by base area

    df = pd.merge(t0, t1, how='inner', left_index=True, right_index=True)
    df.columns = ['x0', 'y0', 'w0', 'h0', 'x1', 'y1', 'w1', 'h1']
    return (abs(df['w0'] * df['h0'] - df['w1'] * df['h1'])) / norm


# Unavoidable movement
def compute_unavoidable_movement(t0, t1):
    base_width = (t0['x'] + t0['w']).max()
    base_height = (t0['y'] + t0['h']).max()
    # Normalize by 4 * hypotenuse
    norm = 4 * math.sqrt(base_width ** 2 + base_height ** 2)

    df = pd.merge(t0, t1, how='inner', left_index=True, right_index=True)
    df.columns = ['x0', 'y0', 'w0', 'h0', 'x1', 'y1', 'w1', 'h1']
    return df.apply(lambda r: unavoidable_travel(*list(r)) / norm, axis=1)


def point_hyperbole_dist(x, w, h, a):
    # Distance between a point (w,h) and a hyperbole y = a/4x where a is the area we are trying to reach
    return math.sqrt((x - w / 2) ** 2 + (a / (4 * x) - h / 2) ** 2)


def unavoidable_travel(*args):
    x0, y0, w0, h0, x1, y1, w1, h1 = args
    if h0 * w0 - w1 * h1 < 0.00001:
        return 0
    else:
        result = scipy.optimize.minimize(point_hyperbole_dist, x0=w0, args=(w0, h0, w1 * h1))
        optimum_x = result.x[0]
        # Minimum corner travel is 4 times the minimum point-hyperbole distance
        return point_hyperbole_dist(optimum_x, w0, h0, w1 * h1) * 4
