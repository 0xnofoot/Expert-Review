package xyz.nofoot.service.impl;

import xyz.nofoot.entity.BlogComments;
import xyz.nofoot.mapper.BlogCommentsMapper;
import xyz.nofoot.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
