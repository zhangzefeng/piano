import scipy
import numpy as np
import matplotlib.pyplot as plt 

def fft_plot(audio, sampling_rate):
	n = len(audio)
	T = 1/sampling_rate
	yf = scipy.fft(audio)
	xf = np.linspace(0.0, 1.0/(2.0*T), n/2)
	fig, ax = plt.subplots()
	yabs = np.abs(yf)
#	yabs = readf()
	ax.plot(xf, 2.0/n * yabs[:n//2])
	plt.grid()
	plt.xlabel("Frequency -->")
	plt.ylabel("Magnitude")
	axes = plt.gca()
	axes.set_xlim(0, 1000)
	return plt.show()

def readf():
	with open('f.dat', 'r') as f:
		yabs = f.read().splitlines()
		return np.asarray([float(i) for i in yabs])

# x axis values 
with open('1/1.dat', 'r') as f:
	audio = f.read().splitlines()
	audio.pop(0)
	# pad to power of 2
	pow2 = 2 ** (int(float(len(audio)).hex().split('p+')[1]) + 1)
	audio += ['0'] * (pow2 - len(audio))
	audio = [float(i) for i in audio]
	fft_plot(audio, 44100)
