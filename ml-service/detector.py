import numpy as np
from sklearn.ensemble import IsolationForest
import joblib
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AnomalyDetector:
    def __init__(self, contamination=0.1):
        self.model = IsolationForest(
            contamination=contamination,
            random_state=42,
            n_estimators=100
        )
        self.is_trained = False
        
    def train(self, data):
        """Train the isolation forest model"""
        try:
            logger.info(f"Training model with {len(data)} samples")
            self.model.fit(data)
            self.is_trained = True
            logger.info("Model training completed")
            return True
        except Exception as e:
            logger.error(f"Training failed: {str(e)}")
            return False
    
    def predict(self, data):
        """Predict anomalies in data"""
        if not self.is_trained:
            logger.warning("Model not trained yet")
            return None
            
        try:
            predictions = self.model.predict(data)
            scores = self.model.score_samples(data)
            
            # Convert predictions: -1 for anomaly, 1 for normal
            anomalies = predictions == -1
            
            return {
                'is_anomaly': anomalies.tolist(),
                'anomaly_scores': scores.tolist()
            }
        except Exception as e:
            logger.error(f"Prediction failed: {str(e)}")
            return None
    
    def save_model(self, path='model.pkl'):
        """Save trained model to disk"""
        try:
            joblib.dump(self.model, path)
            logger.info(f"Model saved to {path}")
            return True
        except Exception as e:
            logger.error(f"Failed to save model: {str(e)}")
            return False
    
    def load_model(self, path='model.pkl'):
        """Load trained model from disk"""
        try:
            self.model = joblib.load(path)
            self.is_trained = True
            logger.info(f"Model loaded from {path}")
            return True
        except Exception as e:
            logger.error(f"Failed to load model: {str(e)}")
            return False
