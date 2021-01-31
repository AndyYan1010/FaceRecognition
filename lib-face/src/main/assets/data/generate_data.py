import numpy as np

t=0
for x in range(10):
    file_name = 'features/feat' + str(x) + '.txt'
    with open(file_name, 'w') as f:
        feat = np.random.rand(1, 512)
        t += 1
        print(str(t) + ": ")
        sum = np.sqrt(np.sum(pow(feat, 2)))
        feat = 1.0*feat/sum
        print(np.sum(pow(feat, 2)))
        for y in range(np.shape(feat)[1]):
            f.write(str(feat[0, y]))
            if y < 511:
                f.write('\n')
