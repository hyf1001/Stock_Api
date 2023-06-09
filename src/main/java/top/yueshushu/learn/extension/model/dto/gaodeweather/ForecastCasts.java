package top.yueshushu.learn.extension.model.dto.gaodeweather;

import lombok.Data;

import java.io.Serializable;

/**
 * 用途描述
 *
 * @author yuejianli
 * @date 2022-06-07
 */
@Data
public class ForecastCasts implements Serializable {
    private String date;
	private String week;
	private String dayweather;
	private String nightweather;
	private String daytemp;
	private String nighttemp;
	private String daywind;
	private String nightwind;
	private String daypower;
	private String nightpower;
}
