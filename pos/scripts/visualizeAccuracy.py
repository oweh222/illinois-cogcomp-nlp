import matplotlib.pyplot as plt
import numpy as np

acc = []
x_axis = []
count = 0

with open("TestAccuracy.txt") as f:
    for line in f:
        acc.append(float(line))
        count += 1
        x_axis.append(count)

plt.plot(x_axis,acc)
plt.axis([1, count, 0.957, 0.967])
plt.title("Accuracy over Testing Set After Each Iteration (Dataset = LDC2015T13)")
plt.xlabel("Number of Iterations")
plt.ylabel("Accuracy")
plt.show()