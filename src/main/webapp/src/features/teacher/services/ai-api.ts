import apiInstance from '../../../services/api-instance';

const aiApi = {
    generateQuestions: async (examId, userPrompt) => {
        const res = await apiInstance.post('/ai/generate-questions', {
            examId: parseInt(examId),
            userPrompt
        });

        return res.data.data;
    },
    
    checkGenerationStatus: async (taskId) => {
        const res = await apiInstance.get(`/ai/status/${taskId}`);
        return res.data;
    }
};

export default aiApi;
