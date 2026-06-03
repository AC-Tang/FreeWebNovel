package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Bookshelves;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.entity.ReadingRecords;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.BookshelvesService;
import com.example.freefiction.mapper.BookshelvesMapper;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.service.sys.ReadingRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Tjianwei
* @description 针对表【bookshelves(书架表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:13
*/
@Service
@RequiredArgsConstructor
public class BookshelvesServiceImpl extends ServiceImpl<BookshelvesMapper, Bookshelves>
    implements BookshelvesService{

    private final ChaptersService chaptersService;
    private final BooksService booksService;
    private final ReadingRecordsService readingRecordsService;

    @Override
    public ServiceResult<List<Bookshelves>> getBookshelvesByUserId(Long userId){
        List<Bookshelves> bookshelves = this.list(new QueryWrapper<Bookshelves>().eq("user_id", userId));
        if ( bookshelves.isEmpty()){
            return ServiceResult.fail("用户书架为空");
        }
        return ServiceResult.ok(bookshelves);
    }

    @Override
    public ServiceResult<Boolean> existInBookshelves(Long userId, Long bookId){
        boolean exist = this.count(new QueryWrapper<Bookshelves>().eq("user_id", userId).eq("book_id", bookId)) > 0;
        if ( !exist){
            return ServiceResult.ok( false);
        }
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Boolean> removeBookFromBookshelves(Bookshelves bookshelves){
        boolean result = this.remove(new QueryWrapper<Bookshelves>().eq("user_id", bookshelves.getUserId()).eq("book_id", bookshelves.getBookId()));
        if ( !result){
            return ServiceResult.fail("将书籍移出书架失败");
        }
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Boolean> addBookToBookshelves(Bookshelves bookshelves){
        bookshelves.setCreatedAt(new Date());
        bookshelves.setUpdatedAt(new Date());
        bookshelves.setItemType(2);
        bookshelves.setParentId(this.getOne(new QueryWrapper<Bookshelves>().eq("user_id", bookshelves.getUserId()).eq("name","默认书架")).getId());
        bookshelves.setSortOrder((int) (this.count(new QueryWrapper<Bookshelves>().eq("user_id", bookshelves.getBookId()).eq("item_type",2).ne("parent_id",null))) + 1);
        bookshelves.setLastReadTime(new Date());
        ReadingRecords readingRecord=readingRecordsService.getOne(new QueryWrapper<ReadingRecords>().eq("user_id", bookshelves.getUserId()).eq("book_id", bookshelves.getBookId()));
        if (readingRecord != null){
            bookshelves.setLastReadChapterId(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", bookshelves.getBookId()).eq("sort_order", readingRecord.getChapterId())).getId());
        }else {
            bookshelves.setLastReadChapterId(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", bookshelves.getBookId()).eq("sort_order", 1)).getId());
        }
        boolean result = this.saveOrUpdate(bookshelves);
        if( !result)
            return ServiceResult.fail("添加失败");
        return ServiceResult.ok(true);
    }

    /**
     * 创建用户书架
     * @param userId 用户ID
     * @return 是否创建成功
     */
    @Override
    public ServiceResult<Boolean> createBookshelves(Long userId, String name){
        return createBookshelf(userId, name)?ServiceResult.ok(true):ServiceResult.fail("创建失败");
    }

    /**
     * 获取用户书架名称
     * @param userId 用户ID
     * @return 书架名称列表
     */
    @Override
    public ServiceResult<List<Bookshelves>> getBookshelvesName(Long userId){
        List<Bookshelves> bookshelvesName = this.list(new QueryWrapper<Bookshelves>().eq("user_id", userId).eq("item_type",1))
                .stream().map(bookshelves -> new Bookshelves(
                bookshelves.getId(),
                bookshelves.getName(),
                bookshelves.getDescription()
        )).collect(Collectors.toList());
        return bookshelvesName.isEmpty()?ServiceResult.fail("用户书架为空"):ServiceResult.ok(bookshelvesName);
    }

    /**
     * 根据书架获取书架书籍
     * @param userId 用户ID
     *               书架ID
     * @return 书架书籍列表
     */
    @Override
    public ServiceResult<List<Bookshelves>> getBookshelvesByName(Long userId, Long id){
        List<Bookshelves> bookshelves = this.list(new QueryWrapper<Bookshelves>().eq("user_id", userId).eq("item_type",2).eq("parent_id", id))
                .stream().map(bookshelf -> new Bookshelves(
                        bookshelf.getBookId(),
                        bookshelf.getLastReadChapterId(),
                        bookshelf.getLastReadTime()
                )).collect(Collectors.toList());

        bookshelves.forEach(bookshelf -> {
            bookshelf.setBookName(booksService.getBooksById(bookshelf.getBookId()).data().getTitle());
            bookshelf.setLastChapterName(chaptersService.getOne(new QueryWrapper<Chapters>().eq("id", bookshelf.getLastReadChapterId())).getTitle());
            bookshelf.setAuthorName(booksService.getBooksById(bookshelf.getBookId()).data().getAuthor());
            bookshelf.setBookCover(booksService.getBooksById(bookshelf.getBookId()).data().getCover());
            bookshelf.setLastUpdateTime(booksService.getBooksById(bookshelf.getBookId()).data().getLastChapterTime());
            bookshelf.setStatusName(booksService.getBooksById(bookshelf.getBookId()).data().getStatusName());
            bookshelf.setCategoryName(booksService.getBooksById(bookshelf.getBookId()).data().getCategoryName());
            bookshelf.setLastReadChapterId(Long.valueOf(chaptersService.getOne(new QueryWrapper<Chapters>().eq("id", bookshelf.getLastReadChapterId())).getSortOrder()));
        });
        return bookshelves.isEmpty()?ServiceResult.fail("用户书架为空"):ServiceResult.ok(bookshelves);
    }

    /**
     * 更新书架书籍信息(名字和简介）
     * @param bookshelves 书架信息
     * @return 是否更新成功
     */
    @Override
    public ServiceResult<Boolean> updateBookshelves(Bookshelves bookshelves){
        return this.updateById(bookshelves)?ServiceResult.ok(true):ServiceResult.fail("更新失败");
    }

    /**
     * 删除书架
     * @param bookshelvesId 书架ID
     * @return 是否删除成功
     */
    @Override
    public ServiceResult<Boolean> deleteBookshelves(Long bookshelvesId){
        return this.removeById(bookshelvesId)?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }

    /**
     * 将书籍移动到书架
     * @param booksId 书籍ID列表
     * @param bookshelvesId 目标书架ID
     * @return 是否移除成功
     */
    @Override
    public ServiceResult<Boolean> moveBookToBookshelves(Long booksId, Long bookshelvesId, Long userId){
        return this.update(new UpdateWrapper<Bookshelves>().eq("user_id", userId).eq("book_id", booksId).set("parent_id", bookshelvesId))?ServiceResult.ok(true):ServiceResult.fail("移除失败");
    }

    public Boolean createBookshelf (Long userId, String name){
        Bookshelves bookshelves = new Bookshelves();
        bookshelves.setUserId(userId);
        bookshelves.setName(name);
        bookshelves.setDescription(name);
        bookshelves.setCreatedAt(new Date());
        bookshelves.setUpdatedAt(new Date());
        bookshelves.setItemType(1);
        return this.saveOrUpdate(bookshelves);
    }
}




