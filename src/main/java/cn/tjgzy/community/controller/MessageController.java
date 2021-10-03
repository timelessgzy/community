package cn.tjgzy.community.controller;

import cn.tjgzy.community.entity.Message;
import cn.tjgzy.community.entity.Page;
import cn.tjgzy.community.entity.User;
import cn.tjgzy.community.service.MessageService;
import cn.tjgzy.community.service.UserService;
import cn.tjgzy.community.util.CommunityUtil;
import cn.tjgzy.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author GongZheyi
 * @create 2021-10-01-17:38
 */
@Controller
public class MessageController {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),
                page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        for (Message message: conversationList) {
            Map<String,Object> map = new HashMap<>();
            map.put("conversation",message);
            map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
            map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
            int fromId = message.getFromId();
            int targetId = user.getId() == fromId ? message.getToId() : fromId;
            User userById = userService.findUserById(targetId);
            map.put("target",userById);
            conversations.add(map);
        }
        model.addAttribute("conversations",conversations);
        // 查询所有未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page
                                  ,Model model) {

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail");
        page.setRows(messageService.findLetterCount(conversationId));

        // 查询私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        for (Message message: letterList) {
            Map<String,Object> map = new HashMap<>();
            map.put("letter",message);
            map.put("fromUser", userService.findUserById(message.getFromId()));
            letters.add(map);
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        for (Message message: letterList) {
            // 如果是未读的消息，并且是接收者
            if (message.getStatus() == 0 && hostHolder.getUser().getId() == message.getToId()) {
                ids.add(message.getId());
            }
        }
        return ids;
    }



    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setCreateTime(new Date());
        message.setContent(content);
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/letter/delete/{messageId}")
    public String deleteMessage(@PathVariable int messageId) {
        int row = messageService.deleteMessage(messageId);
        if (row >= 1) {
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1,"删除失败，请稍候重试");
        }
    }

}
