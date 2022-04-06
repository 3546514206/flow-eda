package com.flow.eda.web.flow;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FlowMapper {

    @Select("SELECT * FROM eda_flow WHERE id=#{id}")
    Flow findById(Long id);

    @Select(
            "<script>SELECT * FROM eda_flow WHERE 1=1 "
                    + "<if test='status!=null'>AND `status`=${status}</if>"
                    + "<if test='name!=null'> AND `name` LIKE '%${name}%'</if></script>")
    List<Flow> findByRequest(FlowRequest request);

    @Insert(
            "INSERT INTO eda_flow (`name`,description,`status`,create_date,update_date) VALUES(#{name},#{description},#{status},#{createDate},#{updateDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Flow flow);

    @Update(
            "<script>UPDATE eda_flow SET <if test='name!=null'>`name`=#{name},</if>"
                    + "<if test='description!=null'>description=#{description},</if>"
                    + "<if test='status!=null'>`status`=#{status},</if>"
                    + "update_date=#{updateDate} WHERE id=#{id}</script>")
    void update(Flow flow);

    @Delete(
            "<script>DELETE FROM eda_flow WHERE id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    void deleteByIds(List<Long> ids);
}