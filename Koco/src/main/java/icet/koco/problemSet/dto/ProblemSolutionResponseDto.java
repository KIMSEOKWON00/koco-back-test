package icet.koco.problemSet.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
@Builder
public class ProblemSolutionResponseDto {
    private String bojUrl;
    private Long problemNumber;
    private String tier;
    private String title;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer submissionCnt;
    private Integer answerCnt;
    private Integer correctPplCnt;
    private Double correctRate;
    private String description;
    private String inputDescription;
    private String outputDescription;
    private String inputExample;
    private String outputExample;
    private String problemCheck;
    private String algorithm;
    private String problemSolving;
    private Map<String, String> solutionCode;
}
