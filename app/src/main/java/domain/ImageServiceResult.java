package domain;

import java.util.List;

public class ImageServiceResult {
    private String success;
    private String created;
    private String errorMessage;
    private List<ProcessedImage> processedImages;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<ProcessedImage> getProcessedImages() {
        return processedImages;
    }

    public void setProcessedImages(List<ProcessedImage> processedImages) {
        this.processedImages = processedImages;
    }
}
