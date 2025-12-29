import grpc
from concurrent import futures
import numpy as np
import logging
import sys
import os

# Add proto generated files to path
sys.path.append(os.path.join(os.path.dirname(__file__), '../proto'))

import detection_service_pb2
import detection_service_pb2_grpc
from detector import AnomalyDetector

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AnomalyDetectionServicer(detection_service_pb2_grpc.AnomalyDetectionServiceServicer):
    def __init__(self):
        self.detector = AnomalyDetector(contamination=0.1)
        # Load pre-trained model if exists
        if os.path.exists('model.pkl'):
            self.detector.load_model('model.pkl')
        logger.info("AnomalyDetectionServicer initialized")
    
    def DetectAnomaly(self, request, context):
        try:
            logger.info(f"Received detection request for event_id: {request.event_id}")
            
            # Convert features to numpy array
            features = np.array(request.features).reshape(1, -1)
            
            # Perform prediction
            result = self.detector.predict(features)
            
            if result is None:
                context.set_code(grpc.StatusCode.FAILED_PRECONDITION)
                context.set_details('Model not trained')
                return detection_service_pb2.AnomalyResponse()
            
            is_anomaly = result['is_anomaly'][0]
            score = result['anomaly_scores'][0]
            
            response = detection_service_pb2.AnomalyResponse(
                event_id=request.event_id,
                is_anomaly=is_anomaly,
                confidence_score=float(score),
                timestamp=request.timestamp
            )
            
            logger.info(f"Detection result for {request.event_id}: anomaly={is_anomaly}, score={score}")
            return response
            
        except Exception as e:
            logger.error(f"Error during detection: {str(e)}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return detection_service_pb2.AnomalyResponse()

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    detection_service_pb2_grpc.add_AnomalyDetectionServiceServicer_to_server(
        AnomalyDetectionServicer(), server)
    
    server_address = '0.0.0.0:50051'
    server.add_insecure_port(server_address)
    
    logger.info(f"Starting gRPC server on {server_address}")
    server.start()
    
    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        logger.info("Shutting down server...")
        server.stop(0)

if __name__ == '__main__':
    serve()
