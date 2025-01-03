package com.airliftcompany.alp3.firmware.FirmwareFiles;

import java.util.List;

/* loaded from: classes.dex */
public class FirmwarePackage {
    public List<Controller> Controllers;
    public long Identifier;
    public String Parameters = null;

    public void Validate() throws PackageException {
        int size = this.Controllers.size();
        Controller GetEcuController = GetEcuController();
        Controller GetDisplayController = GetDisplayController();
        if (size < 1 || size > 2) {
            throw new PackageException("Invalid Controller Count");
        }
        if (GetEcuController == null && GetDisplayController == null) {
            throw new PackageException("Invalid Controller Identifiers");
        }
        if (size == 2 && (GetEcuController == null || GetDisplayController == null)) {
            throw new PackageException("Invalid Controller Types");
        }
        if (GetEcuController != null && GetEcuController.Images.size() != 1) {
            throw new PackageException("Invalid ECU Controller Images");
        }
        if (GetDisplayController != null && GetDisplayController.Images.size() != 2) {
            throw new PackageException("Invalid Display Controller Exceptions");
        }
        ValidateMemory();
    }

    private void ValidateMemory() throws PackageException {
        for (int i = 0; i < this.Controllers.size(); i++) {
            Controller controller = this.Controllers.get(i);
            for (int i2 = 0; i2 < controller.Images.size(); i2++) {
                Image image = controller.Images.get(i2);
                for (int i3 = 0; i3 < image.Segments.size(); i3++) {
                    Segment segment = image.Segments.get(i3);
                    if (segment.Address < image.Address || segment.Size != segment.Data.length || segment.Address + segment.Size > image.Address + image.Size) {
                        throw new PackageException("Segment Contained Invalid Address Data.");
                    }
                }
            }
        }
    }

    public Controller GetEcuController() {
        for (int i = 0; i < this.Controllers.size(); i++) {
            if (this.Controllers.get(i).Identifier == 1234) {
                return this.Controllers.get(i);
            }
        }
        return null;
    }

    public Controller GetDisplayController() {
        for (int i = 0; i < this.Controllers.size(); i++) {
            if (this.Controllers.get(i).Identifier == 4321) {
                return this.Controllers.get(i);
            }
        }
        return null;
    }
}
