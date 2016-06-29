#!python

from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.optimizers import SGD
from keras.utils import np_utils
import numpy as np
import cleartk_io as ctk_io
import sys
import os.path

nb_epoch = 10
batch_size = 64


def main(args):
    if len(args) < 1:
        sys.stderr.write("Error - one required argument: <data directory>\n")
        sys.exit(-1)

    working_dir = args[0]
    
    print("Reading data from %s..." % working_dir)
    
    Y, X = ctk_io.read_liblinear(working_dir)
        
    num_outputs = Y.shape[-1]
    num_examples, dimension = X.shape
    num_y_examples, num_labels = Y.shape
    assert num_examples == num_y_examples
    
    model = Sequential()
    sgd = SGD(lr=0.1, decay=1e-6, momentum=0.9, nesterov=True)
    
    # Dense(10) is a fully-connected layer with 10 hidden units.
    # in the first layer, you must specify the expected input data shape:
    # here, 20-dimensional vectors.
    model.add(Dense(10, input_dim=dimension, init='uniform'))
    model.add(Activation('relu'))
    
    model.add(Dense(10, init='uniform'))
    model.add(Activation('relu'))
    
    if num_outputs > 1:
        model.add(Dense(num_outputs, init='uniform'))
        model.add(Activation('softmax'))                
        model.compile(loss='categorical_crossentropy',
                      optimizer=sgd,
                      metrics=['accuracy'])
    else:
        model.add(Dense(1, init='uniform'))
        model.add(Activation('sigmoid'))
        model.compile(loss='binary_crossentropy',
                      optimizer=sgd,  
                      metrics=['accuracy'])
    print("Shape of y is %s, shape of X is %s, max value in y is %f and min is %f with %d outcomes" % (str(Y.shape), str(X.shape), Y.max(), Y.min(), num_outputs) )
    model.fit(X, Y,
                  nb_epoch=nb_epoch,
                  batch_size=batch_size,
                  verbose=1)
                  
    model.summary()
    
    json_string = model.to_json()
    open(os.path.join(working_dir, 'model_0.json'), 'w').write(json_string)
    model.save_weights(os.path.join(working_dir, 'model_0.h5'), overwrite=True)

if __name__ == "__main__":
    main(sys.argv[1:])
    