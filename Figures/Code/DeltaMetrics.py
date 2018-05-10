import numpy as np

import Metrics
import Parser
import Globals
import MatrixPlot
import TimeBoxplot

technique_list = Parser.list_techniques()


# from the eurovis proposal
#  ratio = (1-delta_vis)/(1-delta_data)
def delta_ratio_matrix(dataset_ids):
    matrix = []
    for dataset_id in dataset_ids:
        dataset_values = []
        for technique_id in technique_list:
            history = Parser.parse_rectangles(technique_id, dataset_id)
            all_ratios = np.array([])
            for revision in range(len(history) - 1):
                delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])
                delta_data = Metrics.compute_delta_data(history[revision], history[revision + 1])
                ratio = (1 - delta_vis) / (1 - delta_data)
                all_ratios = np.append(all_ratios, ratio.values)

            dataset_values.append(all_ratios.mean())
            print(Globals.acronyms[technique_id], dataset_id, all_ratios.mean())
        matrix.append(dataset_values)

    matrix = np.array(matrix).transpose()

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=True,
                    title='Delta ratio')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=True,
                    title='Delta ratio')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=False,
                    title='Delta ratio')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=False,
                    title='Delta ratio')


def delta_ratio_boxplots(dataset_id):
    data = []
    for i, technique_id in enumerate(technique_list):
        technique_data = []
        history = Parser.parse_rectangles(technique_id, dataset_id)
        for revision in range(len(history) - 1):
            delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])
            delta_data = Metrics.compute_delta_data(history[revision], history[revision + 1])
            ratios = (1 - delta_vis) / (1 - delta_data)
            technique_data.append(ratios)
        data.append(technique_data)

    TimeBoxplot.plot(data, technique_list,
                     title="Delta Ratio - " + dataset_id)

    TimeBoxplot.plot(data, technique_list,
                     median_sorted=True,
                     title="Delta Ratio - " + dataset_id)



#  mod = 1 - abs(delta_vis - delta_data)
def delta_diff_matrix(dataset_ids):
    matrix = []
    for dataset_id in dataset_ids:
        dataset_values = []
        for technique_id in technique_list:
            history = Parser.parse_rectangles(technique_id, dataset_id)
            all_diffs = np.array([])
            for revision in range(len(history) - 1):
                delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])
                delta_data = Metrics.compute_delta_data(history[revision], history[revision + 1])
                diffs = 1 - abs(delta_vis - delta_data)
                all_diffs = np.append(all_diffs, diffs.values)

            dataset_values.append(all_diffs.mean())
            print(Globals.acronyms[technique_id], dataset_id, all_diffs.mean())
        matrix.append(dataset_values)

    matrix = np.array(matrix).transpose()

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=True,
                    title='Delta diff')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=True,
                    title='Delta diff')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=False,
                    title='Delta diff')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=False,
                    title='Delta diff')


def delta_diff_boxplots(dataset_id):
    data = []
    for i, technique_id in enumerate(technique_list):
        technique_data = []
        history = Parser.parse_rectangles(technique_id, dataset_id)
        for revision in range(len(history) - 1):
            delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])
            delta_data = Metrics.compute_delta_data(history[revision], history[revision + 1])
            diffs = 1 - abs(delta_vis - delta_data)
            technique_data.append(diffs)
        data.append(technique_data)

    TimeBoxplot.plot(data, technique_list,
                     title="Delta Diff - " + dataset_id)

    TimeBoxplot.plot(data, technique_list,
                     median_sorted=True,
                     title="Delta Diff - " + dataset_id)
