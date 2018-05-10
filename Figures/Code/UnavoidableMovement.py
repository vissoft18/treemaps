import numpy as np

import Metrics
import Parser
import Globals
import MatrixPlot
import TimeBoxplot

#technique_list = ['SliceAndDice', 'SquarifiedTreeMap']
technique_list = Parser.list_techniques()


def plot_time_boxplot(dataset_id):
    # data 1st level = technique, 2nd = list of revisions, 3rd = list of observations
    data = []
    for i, technique_id in enumerate(technique_list):
        technique_results = []
        history = Parser.parse_rectangles(technique_id, dataset_id)
        for revision in range(len(history) - 1):
            un_mov = Metrics.compute_unavoidable_movement(history[revision], history[revision + 1])
            delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])

            diff = 1 - (delta_vis - un_mov)
            technique_results.append(diff)

        data.append(technique_results)

    TimeBoxplot.plot(data, technique_list,
                     title="Unavoidable Movement - " + dataset_id)

    TimeBoxplot.plot(data, technique_list,
                     median_sorted=True,
                     title="Unavoidable Movement - " + dataset_id)


def unavoidable_matrix(dataset_ids):
    matrix = []
    for dataset_id in dataset_ids:
        dataset_values = []
        for technique_id in technique_list:
            history = Parser.parse_rectangles(technique_id, dataset_id)
            all_unavoidable = np.array([])
            for revision in range(len(history) - 1):
                un_mov = Metrics.compute_unavoidable_movement(history[revision], history[revision + 1])
                delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])

                diff = 1 - (delta_vis - un_mov)
                all_unavoidable = np.append(all_unavoidable, diff.values)

            dataset_values.append(all_unavoidable.mean())
            print(Globals.acronyms[technique_id], dataset_id, all_unavoidable.mean())
        matrix.append(dataset_values)

    matrix = np.array(matrix).transpose()

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=True,
                    title='Unavoidable')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=True,
                    title='Unavoidable')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=False,
                    cell_text=False,
                    title='Unavoidable')

    MatrixPlot.plot(matrix, dataset_ids, technique_list,
                    shared_cm=True,
                    cell_text=False,
                    title='Unavoidable')
