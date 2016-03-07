import matplotlib.pyplot as plt
from operator import methodcaller
numOfDocs = 24221

def plot(lines, nrOfPoints, density, alg, section, row, yrange):
	n = 0 if section == "top" else 1
	data = [float(line.split()[n]) for line in lines]
	plt.subplot(2,5,alg + row*5)
	plt.plot([(density*i/numOfDocs) for i in range(nrOfPoints)], data)
	title = "mc"+str(alg)+" "+section+" 50"
	plt.ylim([0,yrange])
	if alg == 1:
		plt.ylabel("sum of absolute difference")
	if row == 1:
		plt.xlabel("N x number of documents")
	plt.title(title)

for (density, nrOfPoints, name) in [(100.0, 4800, "dense20"), (float(numOfDocs), 2000, "sparse2000")]:
	for (section, row, yrange) in [("top",0,0.45),("bottom",1,0.0011)]:
		for i in [1,2,3,4,5]:
			fileName = "mc"+str(i)+name+".txt"
			with open(fileName) as f:
				lines = f.readlines()[:nrOfPoints]
				plot(lines, nrOfPoints, density, i, section, row, yrange)
			
	plt.show()
		
		


