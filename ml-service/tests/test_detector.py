import unittest
import numpy as np
from unittest.mock import patch, MagicMock
import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from detector import AnomalyDetector

class TestAnomalyDetector(unittest.TestCase):
    
    def setUp(self):
        """Set up test fixtures"""
        self.detector = AnomalyDetector(contamination=0.1)
        self.sample_data = np.array([[1.0, 2.0, 3.0],
                                      [1.1, 2.1, 3.1],
                                      [0.9, 1.9, 2.9],
                                      [1.0, 2.0, 3.0],
                                      [10.0, 20.0, 30.0]])  # Outlier
    
    def test_initialization(self):
        """Test detector initialization"""
        self.assertIsNotNone(self.detector)
        self.assertFalse(self.detector.is_trained)
        self.assertEqual(self.detector.model.contamination, 0.1)
    
    def test_train_success(self):
        """Test successful model training"""
        result = self.detector.train(self.sample_data)
        self.assertTrue(result)
        self.assertTrue(self.detector.is_trained)
    
    def test_train_with_insufficient_data(self):
        """Test training with insufficient data"""
        insufficient_data = np.array([[1.0, 2.0]])
        result = self.detector.train(insufficient_data)
        self.assertFalse(result)
    
    def test_predict_before_training(self):
        """Test prediction before model is trained"""
        test_data = np.array([[1.0, 2.0, 3.0]])
        result = self.detector.predict(test_data)
        self.assertIsNone(result)
    
    def test_predict_after_training(self):
        """Test successful prediction after training"""
        self.detector.train(self.sample_data)
        test_data = np.array([[1.0, 2.0, 3.0]])
        result = self.detector.predict(test_data)
        
        self.assertIsNotNone(result)
        self.assertIn('is_anomaly', result)
        self.assertIn('anomaly_scores', result)
        self.assertEqual(len(result['is_anomaly']), 1)
        self.assertEqual(len(result['anomaly_scores']), 1)
    
    def test_predict_detects_outlier(self):
        """Test that obvious outliers are detected"""
        self.detector.train(self.sample_data)
        outlier = np.array([[100.0, 200.0, 300.0]])
        result = self.detector.predict(outlier)
        
        self.assertTrue(result['is_anomaly'][0])
    
    def test_predict_normal_data(self):
        """Test that normal data is not flagged as anomaly"""
        self.detector.train(self.sample_data)
        normal_data = np.array([[1.05, 2.05, 3.05]])
        result = self.detector.predict(normal_data)
        
        self.assertFalse(result['is_anomaly'][0])
    
    @patch('joblib.dump')
    def test_save_model(self, mock_dump):
        """Test model saving"""
        self.detector.train(self.sample_data)
        result = self.detector.save_model('test_model.pkl')
        
        self.assertTrue(result)
        mock_dump.assert_called_once()
    
    @patch('joblib.load')
    def test_load_model(self, mock_load):
        """Test model loading"""
        mock_model = MagicMock()
        mock_load.return_value = mock_model
        
        result = self.detector.load_model('test_model.pkl')
        
        self.assertTrue(result)
        self.assertTrue(self.detector.is_trained)
        mock_load.assert_called_once_with('test_model.pkl')
    
    def test_predict_with_invalid_shape(self):
        """Test prediction with incorrectly shaped data"""
        self.detector.train(self.sample_data)
        invalid_data = np.array([[1.0, 2.0]])  # Wrong number of features
        
        result = self.detector.predict(invalid_data)
        self.assertIsNone(result)
    
    def test_batch_prediction(self):
        """Test batch prediction"""
        self.detector.train(self.sample_data)
        batch_data = np.array([[1.0, 2.0, 3.0],
                               [1.1, 2.1, 3.1],
                               [100.0, 200.0, 300.0]])
        result = self.detector.predict(batch_data)
        
        self.assertEqual(len(result['is_anomaly']), 3)
        self.assertEqual(len(result['anomaly_scores']), 3)

if __name__ == '__main__':
    unittest.main()
