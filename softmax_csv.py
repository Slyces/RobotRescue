#!/usr/bin/env python3
from functools import reduce
import csv, numpy as np

actions = ("Random Walk", "Extinguish Fire", "Resupply Water")
states = [("W", 2), ("F", 2)]
sum_states = [1 if i == len(states) - 1 else sum(map(lambda x: x[1], states[i+1:])) for i in range(len(states))]

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_file", help="put the csv file path")
    args = parser.parse_args()
    csv_path = args.csv_file
    out_path = csv_path.replace(".csv", ".tex")
    out_path = '/'.join(out_path.split('/')[:-1] + ['softmax_' + out_path.split('/')[-1]])


    n_states = reduce(lambda x,y: x*y, map(lambda x: x[1], states))
    n_actions = len(actions)

    table = [[0 for i in range(n_actions)] for j in range(n_states)]
    with open(csv_path) as csvfile:
        reader = csv.reader(csvfile)
        for i, row in enumerate(reader):
            if i < n_states:
                for j, val in enumerate(row[:-1]):
                    table[i][j] = float(val)

    def softmax(x, beta = 8):
        sum_x = sum([np.exp(y * beta) for y in x])
        return [np.exp(y * beta) / sum_x for y in x]

    table = [softmax(row) for row in table]

    lines = [["" for i in range(len(states) + len(actions))] for j in range(n_states)]
    sep = [r"\hline" for i in range(n_states)]

    for i in range(n_states):
        for j, (state, _) in enumerate(states):
            if i % sum_states[j] == 0:
                if _ == 2:
                    text = "${}{}$".format(r'\neg ' if (i // sum_states[j]) % 2 == 0  else '', state)
                else:
                    text = "${} = {}$".format(state, (i // sum_states[j]) % _)
                lines[i][j] = r"\multirow{" + str(sum_states[j]) + r"}{*}{" + text + r"}"
                if j != len(states) - 1 and sep[i] == r"\hline":
                    sep[i] = r'\cline{' + str(j + 2)  + '-' + str(len(lines[0])) + '}'
                else:
                    lines[i][j] = text

        for j, val in enumerate(table[i]):
            lines[i][len(states) + j] = "{:.2f}".format(float(val))

    header = [' '] * len(states) + list(actions)

    indent = "  "
    tex = ""
    # tex = r"\begin{code}" + "\n"
    tex += r"\begin{center}" + "\n"
    tex += r"\begin{tabular}{|" + "|".join(['c' for i in range(len(lines[0]))]) + "|}\n"
    tex += indent + r"\hline" + "\n"
    tex += ' & '.join(header) + r"\\" + "\n"
    tex += indent + r"\hline" + "\n"
    for i in range(n_states):
        tex += indent + " & ".join(lines[i]) + r"\\" + "\n"
        if i != n_states - 1:
            tex += indent + sep[i] + "\n"
    tex += indent + r"\hline" + "\n"
    tex += r"\end{tabular}" + "\n"
    tex += r"\end{center}" + "\n"

    open(out_path, 'w').write(tex)
