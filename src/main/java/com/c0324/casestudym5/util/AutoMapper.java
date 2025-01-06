package com.c0324.casestudym5.util;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AutoMapper {

    CommonMapper INSTANCE = Mappers.getMapper(CommonMapper.class);

}
