package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTagListResponseDTO {
    private List<TagItem> tagList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagItem {
        private String tagId;
        private String tagName;
        private String tagDesc;
        private Integer statistics;
    }
}
