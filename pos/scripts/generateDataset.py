training_begin = 0
training_end = 18
dev_begin = 19
dev_end = 21
test_begin = 22
test_end = 24
train_dev_begin = 0
train_dev_end = 21

in_folder = "wsj/"

out_folder = "out/"
train_file = out_folder + "00-18.txt"
dev_file = out_folder + "19-21.txt"
test_file = out_folder + "22-24.txt"
train_dev_file = out_folder + "00-21.txt"

# Generate Training Set
filename = ''
print("Generating Training Set...")
with open(train_file, 'w+') as out_f:
    for x in range(training_begin, training_end + 1):
        if x < 10:
            filename = in_folder + 'wsj-0' + str(x)
        else:
            filename = in_folder + 'wsj-' + str(x)

        with open(filename) as in_f:
            for line in in_f:
                out_f.write(line)

# Generate Dev Set
print("Generating Dev Set...")

with open(dev_file, 'w+') as out_f:
    for x in range(dev_begin, dev_end + 1):
        if x < 10:
            filename = in_folder + 'wsj-0' + str(x)
        else:
            filename = in_folder + 'wsj-' + str(x)

        with open(filename) as in_f:
            for line in in_f:
                out_f.write(line)

# Generate Test Set
print("Generating Test Set...")

with open(test_file, 'w+') as out_f:
    for x in range(test_begin, test_end + 1):
        if x < 10:
            filename = in_folder + 'wsj-0' + str(x)
        else:
            filename = in_folder + 'wsj-' + str(x)

        with open(filename) as in_f:
            for line in in_f:
                out_f.write(line)

# Generate Dev + Train Set
print("Generating Training + Dev Set...")

with open(train_dev_file, 'w+') as out_f:
    for x in range(train_dev_begin, train_dev_end + 1):
        if x < 10:
            filename = in_folder + 'wsj-0' + str(x)
        else:
            filename = in_folder + 'wsj-' + str(x)

        with open(filename) as in_f:
            for line in in_f:
                out_f.write(line)
