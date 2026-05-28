package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class AdminTagUpsertRequestDTO {
    private String tagId;
    private String tagName;
    private String tagDesc;
}
