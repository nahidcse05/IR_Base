package Classifier;
import java.util.Collection;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import structures._Corpus;
import structures._Doc;
import structures._SparseFeature;

public class SVM extends BaseClassifier{
	svm_parameter m_param; //Define it to be global variable.
	svm_model m_model;
	
	//Constructor without give C.
	public SVM(_Corpus c, int classNumber, int featureSize){
		super(c, classNumber, featureSize);
		//Set default value of the param.
		m_param = new svm_parameter();
		m_param.svm_type = svm_parameter.C_SVC;
		m_param.kernel_type = svm_parameter.LINEAR; // Hongning: linear kernel is the general choice for text classification
		m_param.degree = 1;
		m_param.gamma = 0; // 1/num_features
		m_param.coef0 = 0.2;
		m_param.nu = 0.5;
		m_param.cache_size = 100;
		m_param.C = 1;
		m_param.eps = 1e-6;
		m_param.p = 0.1;
		m_param.shrinking = 1;
		m_param.probability = 0;
		m_param.nr_weight = 0;
		m_param.weight_label = new int[0];// Hongning: why set it to 0-length array??
		m_param.weight = new double[0];
	}

	//Constructor with a given C.
	public SVM(_Corpus c, int classNumber, int featureSize, double C){
		super(c, classNumber, featureSize);
		// Set default value of the param.
		m_param = new svm_parameter();
		m_param.svm_type = svm_parameter.C_SVC;
		m_param.kernel_type = svm_parameter.LINEAR; // Hongning: linear kernel is thegeneral choice for text classification
		m_param.degree = 1;
		m_param.gamma = 0; // 1/num_features
		m_param.coef0 = 0.2;
		m_param.nu = 0.5;
		m_param.cache_size = 100;
		m_param.C = C;
		m_param.eps = 1e-6;
		m_param.p = 0.1;
		m_param.shrinking = 1;
		m_param.probability = 0;
		m_param.nr_weight = 0;
		m_param.weight_label = new int[0];// Hongning: why set it to 0-length array??
		m_param.weight = new double[0];
	}
	
	@Override
	public String toString() {
		return String.format("SVM[C:%d, F:%d, T:%d]", m_classNo, m_featureSize, m_param.svm_type);
	}
	
	@Override
	protected void init() {
		//no need to initiate, libSVM will take care of it
	}
	
	protected svm_node[] createSample(_Doc doc) {
		svm_node[] node = new svm_node[doc.getDocLength()]; 
		int fid = 0;
		for(_SparseFeature fv:doc.getSparse()){
			node[fid] = new svm_node();
			node[fid].index = 1 + fv.getIndex();//svm's feature index starts from 1
			node[fid].value = fv.getValue();
			fid ++;
		}
		return node;
	}
	
	@Override
	public void train(Collection<_Doc> trainSet) {
		svm_problem problem = new svm_problem();
		problem.x = new svm_node[trainSet.size()][];
		problem.y = new double [trainSet.size()];		
		
		//Construct the svm_problem by enumerating all docs.
		int docId = 0;
		for(_Doc doc : trainSet){
			problem.x[docId] = createSample(doc);
			problem.y[docId] = doc.getYLabel();
			docId ++;
		}	
		m_param.gamma = 1.0/m_featureSize;//Set the gamma of parameter.
		problem.l = docId;
		m_model = svm.svm_train(problem, m_param);
	}
	
	@Override
	public int predict(_Doc doc) {
		return (1+(int)svm.svm_predict(m_model, createSample(doc)))/2;
	}
}
